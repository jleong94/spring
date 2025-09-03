package com.api.firebase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.firebase.fcm.Message;

import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class FirebaseTests {

	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Test
	@Order(1)
	void testSendTokenBasedPushNotification() throws Exception {
		MDC.put("mdcId", UUID.randomUUID());
		log.info("-Test send token based push notification start-");
		try {
			Message message = Message.builder()
					.token(List.of("dummy-token"))
					.title("Test Notification")
					.body("This is a test push notification")
					.build();
			// 🔹 Perform POST request and assert 200 OK
			mockMvc.perform(post("/v1/send/token-based/notification")
					.with(jwt().jwt(jwt -> jwt
							.claim("scope", "superadmin_fcm_write") // simulate Keycloak "scope"
							).authorities(new SimpleGrantedAuthority("SCOPE_superadmin_fcm_write"))) // ✅ injects mock JWT with the right authority
					.header("mdcId", "test-mdc-123")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(message)))
			.andExpect(status().isOk())   // ✅ Expect HTTP 200
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
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
		} finally {
			log.info("-Test send token based push notification end-");
			MDC.clear();
		}
	}
}
