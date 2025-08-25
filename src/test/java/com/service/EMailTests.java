package com.service;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.MethodOrderer;
import com.modal.EMail;

import lombok.extern.slf4j.Slf4j;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@Transactional
@SpringBootTest
public class EMailTests {
	
	@Autowired
	EMailService eMailService;

	@Test
	@Order(1)
	void testSendEmail() throws Exception {
		try {
			EMail email = EMail.builder()
					.receiver("james.leong@mpsb.net")
					.subject("Test EMail")
					.body("Hello world.")
					.isHTML(false)
					.build();
			eMailService.sendEMail(log, email);
		} catch(Exception e) {
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
