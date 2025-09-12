package com.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.modal.Email;
import com.modal.EmailAttachment;
import com.pojo.Property;
import com.repo.EmailRepo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.internet.MimeMessage;
@Service
public class EmailService {

	private final JavaMailSender javaMailSender;

	private final EmailRepo emailRepo;

	private final Property property;

	private final MeterRegistry meterRegistry;
	private final Counter recoverCounter;

	private final RestTemplate restTemplate = new RestTemplate();

	public EmailService(JavaMailSender javaMailSender, EmailRepo emailRepo, Property property, MeterRegistry meterRegistry) {
		this.javaMailSender = javaMailSender;
		this.emailRepo = emailRepo;
		this.property = property;
		this.meterRegistry = meterRegistry;
		this.recoverCounter = Counter.builder("email_service_failures_total")
				.description("Number of failed retries hitting @Recover")
				.register(this.meterRegistry);
	}

	public String exceptionNotificationEmailTemplate() {
		return new StringBuilder()
				.append("<!doctype html>")
				.append("<html>")
				.append("<head>")
				.append("  <meta charset=\"utf-8\"/>")
				.append("  <title>Exception Alert \u2014 %s</title>")
				.append("  <style>")
				.append("    body { font-family: Arial, sans-serif; background:#f4f6f8; padding:20px; }")
				.append("    .container { max-width:800px; margin:auto; background:#fff; border:1px solid #ddd; border-radius:6px; padding:20px; }")
				.append("    .header { font-size:16px; font-weight:bold; margin-bottom:12px; color:#d32f2f; }")
				.append("    .message { font-size:14px; margin-bottom:20px; color:#333; line-height:1.5; }")
				.append("    .stack { background:#f7f9fc; border:1px solid #e6eef8; padding:12px; border-radius:4px; font-family: Consolas, \"Courier New\", monospace; font-size:12px; white-space:pre-wrap; overflow:auto; }")
				.append("    .footer { margin-top:20px; font-size:14px; color:#333; line-height:1.5; }")
				.append("  </style>")
				.append("</head>")
				.append("<body>")
				.append("  <div class=\"container\">")
				.append("    <div class=\"header\">🚨 Exception Notification \u2014 %s</div>")
				.append("    <div class=\"message\">")
				.append("      Dear Support Team,<br><br>")
				.append("      An exception has been detected in the application. Please review the stack trace below and take the necessary corrective action at the earliest convenience.")
				.append("    </div>")
				.append("    <div class=\"stack\">")
				.append("      %s")
				.append("    </div>")
				.append("    <div class=\"footer\">")
				.append("      Best regards,<br>")
				.append("      MPay Support")
				.append("    </div>")
				.append("  </div>")
				.append("</body>")
				.append("</html>")

				.toString();
	}

	/*
	 * Send email
	 * @param email
	 * */
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
	@Transactional
	public Email sendEmail(Logger log, Email email) throws Throwable {
		// Regex: looks for any opening/closing tag like <...>
		Pattern TAG_PATTERN = Pattern.compile("<\\s*\\/?[a-zA-Z][^>]*>");
		try {
			email = email.toBuilder().sender(property.getSpring_mail_host()).build();
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			mimeMessageHelper.setFrom(email.getSender()); // must match sender
			if(email.getReplyTo() != null && !email.getReplyTo().isBlank()) {mimeMessageHelper.setReplyTo(email.getReplyTo());}
			if(email.getReceiver() != null && !email.getReceiver().isBlank()) {mimeMessageHelper.setTo(email.getReceiver());}
			if(email.getCc() != null && !email.getCc().isBlank()) {mimeMessageHelper.setCc(email.getCc());}
			if(email.getBcc() != null && !email.getBcc().isBlank()) {mimeMessageHelper.setBcc(email.getBcc());}
			mimeMessageHelper.setSubject(email.getSubject());
			mimeMessageHelper.setText(email.getBody(), TAG_PATTERN.matcher(email.getBody()).find()); // ✅ `true` for HTML
			if(email.getAttachments() != null && email.getAttachments().size() > 0) {
				for(EmailAttachment emailAttachment : email.getAttachments()) {
					Path path = Paths.get(emailAttachment.getFile_path());
					if(Files.exists(path) && Files.isRegularFile(path)) {
						mimeMessageHelper.addAttachment(path.getFileName().toString(), path.toFile());
					}
				}
			}
			javaMailSender.send(mimeMessage);
			email.setSend(true);
			emailRepo.save(email);
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
		} finally {

		}
		return email;
	}

	// @Recover is called when a @Retryable method exhausts all retries.
	// 1st param = exception type to recover from
	// Remaining params = must match the original @Retryable method’s args
	// Acts as the final fallback (not retried again if it fails)
	// Return type must same with @Retryable method
	@Recover
	public Email recover(Throwable throwable, Logger log, Email email) throws Throwable {
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
			}

			email.setSend(true);
			emailRepo.save(email);
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
		return email;
	}
}
