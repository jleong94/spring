package com.service.firebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.enums.ResponseCode;
import com.google.firebase.messaging.*;
import com.modal.Email;
import com.pojo.Property;
import com.service.EmailService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class FirebaseService {

	private final Property property;
	
	private final EmailService emailService;

	private final MeterRegistry meterRegistry;
	private final Counter recoverCounter;

	private final RestTemplate restTemplate = new RestTemplate();

	public FirebaseService(Property property, EmailService emailService, MeterRegistry meterRegistry) {
		this.property = property;
		this.emailService = emailService;
		this.meterRegistry = meterRegistry;
		this.recoverCounter = Counter.builder("firebase_service_failures_total")
				.description("Number of failed retries hitting @Recover")
				.register(this.meterRegistry);
	}

	/**
	 * Builds an APNs configuration object for iOS notifications.
	 *
	 * <p>This config enables "rich media" notifications on iOS by setting
	 * `mutable-content: 1`, which allows the client-side app extension
	 * to process and render additional content such as images.</p>
	 *
	 * @param imageUrl Optional URL pointing to an image to be included
	 *                 in the notification payload (handled by iOS client).
	 * @return A fully built {@link ApnsConfig} instance.
	 */
	private ApnsConfig buildApnsConfig(String imageUrl) {
		// Base APS payload for iOS
		// - "sound: default" => plays default iOS notification sound
		// - "mutable-content: 1" => allows notification service extension to intercept/modify payload
		Aps aps = Aps.builder()
				.setSound("default")
				.setMutableContent(true)
				.build();

		// Create APNs configuration builder with standard headers:
		// - apns-priority: 10 => send immediately (use 5 for background updates)
		// - apns-push-type: alert => required header for alert notifications
		ApnsConfig.Builder builder = ApnsConfig.builder()
				.setAps(aps)
				.putHeader("apns-priority", "10")
				.putHeader("apns-push-type", "alert");

		// You can optionally pass image url in custom data; your iOS app extension fetches it.
		if (imageUrl != null && !imageUrl.isBlank()) {
			Map<String, Object> imageData = new HashMap<>();
			imageData.put("imageUrl", imageUrl);
			builder.putAllCustomData(imageData);
		}
		return builder.build();
	}

	/**
	 * Builds an Android-specific FCM notification configuration.
	 *
	 * <p>This config allows you to customize how notifications behave on Android devices,
	 * including rich media (images) and deep link navigation when the notification is tapped.</p>
	 *
	 * @param imageUrl     Optional image URL to display in the notification
	 *                     (supported on Android 8.0+ with NotificationCompat or system UI).
	 * @param clickAction  Optional action string that maps to an Activity intent filter
	 *                     or deep link (used when the user taps the notification).
	 * @return A fully built {@link AndroidConfig} instance with high-priority delivery.
	 */
	private AndroidConfig buildAndroidConfig(String imageUrl, String clickAction) {
		// Create a builder for the Android notification payload.
		// This is separate from the "data" payload; it controls how
		// the system UI renders the notification.
		AndroidNotification.Builder androidNotif = AndroidNotification.builder();
		// If an image URL is provided, attach it to the notification.
		// The Android system UI will render it as a large picture (if supported).
		if (imageUrl != null && !imageUrl.isBlank()) {
			androidNotif.setImage(imageUrl);
		}
		// If a click action is provided, set it on the notification.
		// - This should match an intent filter in your AndroidManifest.xml,
		//   or a deep link URI that your app can handle.
		// - When the user taps the notification, Android launches the activity
		//   associated with this action.
		if (clickAction != null && !clickAction.isBlank()) {
			androidNotif.setClickAction(clickAction); // e.g. activity intent action or deep link
		}
		// Build the AndroidConfig object:
		// - Priority.HIGH ensures the notification is delivered immediately
		//   (may wake up the device; useful for time-sensitive alerts).
		// - Attach the customized AndroidNotification payload.
		return AndroidConfig.builder()
				.setPriority(AndroidConfig.Priority.HIGH)
				.setNotification(androidNotif.build())
				.build();
	}

	private Map<String, String> safeData(Map<String, String> data) {
		return data == null ? Map.of() : data;
	}

	@Retryable(//Retry the method on exception
			retryFor = { Throwable.class },
			maxAttempts = 3,//Retry up to nth times
			/*
			 * backoff = Delay before each retry
			 * delay = Start with nth seconds
			 * multiplier = Exponential backoff (2s, 4s, 8s...)
			 * */
			backoff = @Backoff(delay = 1000, multiplier = 2)
			)
	public com.pojo.firebase.fcm.Message sendTokenBasedPushNotification(Logger log, com.pojo.firebase.fcm.Message message) throws Throwable {
		try {
			MulticastMessage.Builder builder = MulticastMessage.builder().addAllTokens(message.getToken());

			if((message.getTitle() != null && !message.getTitle().isBlank()) && (message.getBody() != null && !message.getBody().isBlank())) {
				Notification notification = Notification.builder()
						.setTitle(message.getTitle())
						.setBody(message.getBody())
						.build();
				builder.setNotification(notification);
			} if ((message.getImageUrl() != null && !message.getImageUrl().isBlank()) || (message.getClickAction() != null && !message.getClickAction().isBlank())) {
				builder.setAndroidConfig(buildAndroidConfig(message.getImageUrl(), message.getClickAction()));
				builder.setApnsConfig(buildApnsConfig(message.getImageUrl()));
			}

			builder.putAllData(safeData(message.getData()));

			BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());
			List<Map<String, Object>> resp_data = Collections.synchronizedList(new ArrayList<>());
			batchResponse.getResponses().forEach(sendResponse -> 
			resp_data.add(Map.of(
					"resp_code", sendResponse.isSuccessful() 
					? ResponseCode.SUCCESS.getResponse_code() 
							: ResponseCode.FAILED.getResponse_code(),
							"resp_msg", sendResponse.isSuccessful() 
							? ResponseCode.SUCCESS.getResponse_desc() 
									: sendResponse.getException().getMessage(),
									"messageId", sendResponse.getMessageId()
					))
					);
			return message.toBuilder().resp_data(resp_data)
					.success_count(batchResponse.getSuccessCount())
					.fail_count(batchResponse.getFailureCount())
					.build();
		} catch (Throwable e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			throw e;
		}
	}

	// @Recover is called when a @Retryable method exhausts all retries.
	// 1st param = exception type to recover from
	// Remaining params = must match the original @Retryable method’s args
	// Acts as the final fallback (not retried again if it fails)
	// Return type must same with @Retryable method
	@Recover
	public com.pojo.firebase.fcm.Message recover(Throwable throwable, Logger log, com.pojo.firebase.fcm.Message message) throws Throwable {
		log.info("Recover on throwable start.");
		try {
			String error_detail = "";
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			for (StackTraceElement element : throwable.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())) {
					error_detail += (error_detail != null && !error_detail.isBlank() ? "<br>" : "") + String.format("Error in %s at line %d: %s - %s",
							element.getClassName(),
							element.getLineNumber(),
							throwable.getClass().getName(),
							throwable.getMessage());
					break;
				}
			}
			// Increment Prometheus counter
			recoverCounter.increment();

			// Persist failure details

			// Trigger alerts (Ops team, monitoring system)
			if(property.getAlert_slack_webhook_url() != null && !property.getAlert_slack_webhook_url().isBlank()) {
				restTemplate.postForEntity(property.getAlert_slack_webhook_url(), java.util.Collections.singletonMap("text", error_detail), String.class);
			} if((property.getAlert_support_email_to() != null && !property.getAlert_support_email_to().isBlank()) ||
					(property.getAlert_support_email_cc() != null && !property.getAlert_support_email_cc().isBlank()) ||
					(property.getAlert_support_email_bcc() != null && !property.getAlert_support_email_bcc().isBlank())) {
				String exceptionNotificationEmailTemplate = String.format(emailService.exceptionNotificationEmailTemplate(), error_detail);
				Email email = Email.builder()
						.sender(property.getSpring_mail_sender())
						.replyTo(property.getAlert_support_email_replyTo())
						.receiver(property.getAlert_support_email_to())
						.cc(property.getAlert_support_email_cc())
						.bcc(property.getAlert_support_email_bcc())
						.subject("System Exception Error!!!")
						.body(exceptionNotificationEmailTemplate)
						.build();
				emailService.sendEmail(log, email);
			}
		} catch(Throwable e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			throw e;
		} finally{
			log.info("Recover on throwable end.");
		}
		return message.toBuilder().success_count(0)
				.fail_count(0)
				.build();
	}
}
