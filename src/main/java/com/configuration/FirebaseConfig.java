package com.configuration;

import java.io.InputStream;
import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "google.fcm")
@Slf4j
@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@Builder(toBuilder = true)
public class FirebaseConfig {

	@Builder.Default
	private boolean init = false;
	
	private Credentials credentials;

	public FirebaseConfig(Credentials credentials) {
		this.credentials = credentials;
	}

	@Bean
	CommandLineRunner initFirebase() {
		return args -> {
			try {
				if (!FirebaseApp.getApps().isEmpty()) {
	                log.info("Firebase already initialized");
	                init = true; return;
	            }

	            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(credentials.getPath());
	            if (inputStream != null) {
	            	GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream);
	            	log.info("Initializing Firebase with classpath credentials: {}", credentials.getPath());
	            	FirebaseOptions firebaseOptions = FirebaseOptions.builder()
	                        .setCredentials(googleCredentials)
	                        .build();
	                FirebaseApp.initializeApp(firebaseOptions);
	                log.info("Firebase initialized");
					init = true;
	            } else {
	            	log.info("Couldn't find {} on classpath.", credentials.getPath());
	            }
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

	@Bean
	HealthIndicator firebaseHealthIndicator() {
		return () -> {
			if (init) {
				return Health.up().build();
			}
			return Health.down()
					.withDetail("message", "Firebase not initialized.")
					.withDetail("timestamp", Instant.now())
					.build();
		};
	}
	
	@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
    @AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
    @NoArgsConstructor//Generates a constructor with no parameters
    @Builder(toBuilder = true)
    public static class Credentials {
    	
    	@JsonProperty(value = "path")
        private String path;
    }
}
