package com.configuration;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.crypto.tink.apps.paymentmethodtoken.GooglePaymentsPublicKeysManager;
import com.pojo.Property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for Google Pay integration.
 * Handles initialization of Google Pay public keys and provides health monitoring.
 */
@Configuration
@ConfigurationProperties(prefix = "google.pay")
@Slf4j
@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
public class GooglePayConfig {

	@Builder.Default
	private boolean init = false;
	
	private Key key;
	
	@Autowired(required = false)
	private Property property;

	public GooglePayConfig(Property property, Key key) {
		this.property = property;
		this.key = key;
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
	
	@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
    @AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
    @NoArgsConstructor//Generates a constructor with no parameters
    @Builder(toBuilder = true)
    public static class Key {
    	
    	@JsonProperty(value = "path")
        private String path;
    }
}
