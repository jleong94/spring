package com.service;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.MethodOrderer;
import com.modal.Email;
import com.pojo.Property;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.jboss.logging.MDC;

import lombok.extern.slf4j.Slf4j;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({EmailService.class, Property.class}) // Import your service class
@TestPropertySource(locations = "classpath:application-test.yml")
@EnableConfigurationProperties(Property.class)
public class EmailITests {
	
	@Autowired
	private EmailService emailService;
	
	@MockitoBean
	private JavaMailSender mailSender;
	
	@Value("${spring.mail.sender}")
    private String spring_mail_sender;

	@Test
	@Transactional
	@Order(1)
	void testSendEmail() throws Throwable {
		MDC.put("X-Request-ID", UUID.randomUUID());
		log.info("-Test send email start-");
		try {
			// stub createMimeMessage to return a usable object
		    when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
		    
			Email email = Email.builder()
					.sender(spring_mail_sender)
					.receiver("james.leong@mpsb.net")
					.subject("Test Email")
					.body("Hello world.")
					.build();
			// ✅ Assert: result should be true
	        assertTrue(emailService.sendEmail(log, email).isSend(), "sendEmail should return true when email is sent successfully");
			
			// Widely used validation: check send() was called once
			verify(mailSender, times(1)).send(any(MimeMessage.class));
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
			log.info("-Test send email end-");
			MDC.clear();
		}
	}
}
