package com.utilities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.jboss.logging.MDC;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.pojo.ApiKey;

import lombok.extern.slf4j.Slf4j;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ToolITests {
	
	@Autowired
    private Tool tool;

	@Autowired
	ApiKey apiKey;
	
	@Test
	@Order(1)
	void testSignature() throws Throwable {
		MDC.put("X-Request-ID", UUID.randomUUID());
		log.info("-Test signature start-");
		try {
			// Arrange
			String plain = "{\"topic\": \"\",\"message\": \"\"}";

			// Act - Sign the plain text
			String signature = tool.signSHA256RSA(log, apiKey.getGeneral(), plain);

			// Assert - Signature should not be null or empty
			assertNotNull(signature, "Signature should not be null");
			assertFalse(signature.isBlank(), "Signature should not be blank");

			// Act - Verify the signature
			boolean isValid = tool.verifySHA256RSA(log, apiKey.getGeneral(), plain, signature);

			// Assert - Verification should succeed
			assertTrue(isValid, "Signature should be valid for the plain text");

			log.info("Plain text test passed - Signature: {}", signature);
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
			log.info("-Test signature end-");
			MDC.clear();
		}
	}
}
