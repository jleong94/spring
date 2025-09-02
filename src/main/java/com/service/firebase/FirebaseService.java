package com.service.firebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enums.ResponseCode;
import com.google.firebase.messaging.*;
import com.pojo.ApiResponse;
import com.utilities.Tool;

@Service
public class FirebaseService {
	
	@Autowired
	Tool tool;
	
	private ApnsConfig buildApnsConfig(String imageUrl) {
		// For rich media on iOS you typically set "mutable-content":1 and provide image in payload (client renders)
		Aps aps = Aps.builder()
				.setSound("default")
				.setMutableContent(true)
				.build();

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
	
	private AndroidConfig buildAndroidConfig(String imageUrl, String clickAction) {
		AndroidNotification.Builder androidNotif = AndroidNotification.builder();
		if (imageUrl != null && !imageUrl.isBlank()) {
			androidNotif.setImage(imageUrl);
		}
		if (clickAction != null && !clickAction.isBlank()) {
			androidNotif.setClickAction(clickAction); // e.g. activity intent action or deep link
		}
		return AndroidConfig.builder()
				.setPriority(AndroidConfig.Priority.HIGH)
				.setNotification(androidNotif.build())
				.build();
	}
	
	private Map<String, String> safeData(Map<String, String> data) {
        return data == null ? Map.of() : data;
    }

	public ApiResponse send(Logger log, com.pojo.firebase.fcm.Message message) throws Exception {
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
			for (SendResponse sendResponse : batchResponse.getResponses()) {
				resp_data.add(Map.of(
						"resp_code", sendResponse.isSuccessful() ? ResponseCode.SUCCESS.getResponse_code() : ResponseCode.FAILED.getResponse_code(),
								"resp_msg", sendResponse.isSuccessful() ? ResponseCode.SUCCESS.getResponse_desc() : sendResponse.getException().getMessage(),
										"messageId", sendResponse.getMessageId()
						));
			}
			message = message.toBuilder().resp_data(resp_data)
					.success_count(batchResponse.getSuccessCount())
					.fail_count(batchResponse.getFailureCount())
					.build();
			return ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.message(message)
					.build();
		} catch (Exception e) {
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
}
