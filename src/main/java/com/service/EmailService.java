package com.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	JavaMailSender javaMailSender;

	@Autowired
	EmailRepo emailRepo;

	@Autowired
	Property property;

	private final MeterRegistry meterRegistry;
	private final Counter recoverCounter;

	private final RestTemplate restTemplate = new RestTemplate();

	public EmailService(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		this.recoverCounter = Counter.builder("email_service_failures_total")
				.description("Number of failed retries hitting @Recover")
				.register(this.meterRegistry);
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
			mimeMessageHelper.setTo(email.getReceiver() == null ? "" : email.getReceiver());
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
			emailRepo.save(email);
		}
		return email;
	}

	// @Recover is called when a @Retryable method exhausts all retries.
	// 1st param = exception type to recover from
	// Remaining params = must match the original @Retryable method’s args
	// Acts as the final fallback (not retried again if it fails)
	// Not necessary is void return type
	@Recover
	public void recover(Throwable throwable, Logger log, Email email) throws Throwable {
		log.info("Recover on throwable start.");
		try {
			// Increment Prometheus counter
			recoverCounter.increment();

			// Persist failure details

			// Trigger alerts (Ops team, monitoring system)
			if(property.getAlert_slack_webhook_url() != null && !property.getAlert_slack_webhook_url().isBlank()) {
				String error_detail = "";
				StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
				for (StackTraceElement element : throwable.getStackTrace()) {
					if (currentElement.getClassName().equals(element.getClassName())
							&& currentElement.getMethodName().equals(element.getMethodName())) {
						error_detail = String.format("Error in %s at line %d: %s - %s",
								element.getClassName(),
								element.getLineNumber(),
								throwable.getClass().getName(),
								throwable.getMessage());
						break;
					}
				}
				restTemplate.postForEntity(property.getAlert_slack_webhook_url(), java.util.Collections.singletonMap("text", error_detail), String.class);
			}

			//Return a safe fallback value 
			return;
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
	}
}
