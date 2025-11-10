package com.configuration;

import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.crypto.tink.apps.paymentmethodtoken.GooglePaymentsPublicKeysManager;
import com.pojo.Property;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for Google Pay integration.
 * Handles initialization of Google Pay public keys and provides health monitoring.
 */
@Slf4j
@Configuration
public class GooglePayConfig {

	private final Property property;

	private boolean init = false;

	public GooglePayConfig(Property property) {
		this.property = property;
	}

	/**
     * Initializes Google Pay by refreshing public keys based on the active profile.
     * Runs on application startup as a CommandLineRunner.
     * 
     * @return CommandLineRunner that performs the initialization
     */
	@Bean
	CommandLineRunner initGooglePay() {
		return args -> {
			try {
				if(property.getSpring_profiles_active().equalsIgnoreCase("prod")) {GooglePaymentsPublicKeysManager.INSTANCE_PRODUCTION.refreshInBackground();}
				else {GooglePaymentsPublicKeysManager.INSTANCE_TEST.refreshInBackground();}
				init = true;
			} catch (Exception e) {
				init = false;
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
		};
	}

	/**
     * Provides a health indicator for Google Pay initialization status.
     * Used by Spring Boot Actuator to monitor the health of Google Pay integration.
     * 
     * @return HealthIndicator that reports UP if Google Pay is initialized, DOWN otherwise
     */
	@Bean
	HealthIndicator googlePaymentHealthIndicator() {
		return () -> {
			if (init) {
				return Health.up().build();
			}
			return Health.down()
					.withDetail("message", "Google Pay not initialized")
					.withDetail("timestamp", Instant.now())
					.build();
		};
	}
}
