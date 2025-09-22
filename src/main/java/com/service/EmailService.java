package com.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.modal.Email;
import com.modal.EmailAttachment;
import com.pojo.Property;
import com.repo.EmailRepo;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

	public String loadHtmlTemplate(Logger log, String filename) throws Throwable {
		try {
			ClassPathResource resource = new ClassPathResource("templates/html/".concat(filename));
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
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
		}
	}

	/*
	 * Send email
	 * @param email
	 * */
	@Retry(name = "sendEmail")
    @CircuitBreaker(name = "sendEmail", fallbackMethod = "fallbackSendEmail")
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

	// Exception param must put as last param in fallback method
	public Email fallbackSendEmail(Logger log, Email email, Throwable throwable) throws Throwable {
		log.info("Recover on send email start.");
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
			log.info("Recover on send email end.");
		}
		return email;
	}
}
