package com.service.google;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pojo.Property;
import com.service.EmailService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class GooglePayService {

	private final Property property;
	
	private final EmailService emailService;

	private final MeterRegistry meterRegistry;
	
	private final Counter recoverCounter;

	private final RestTemplate restTemplate = new RestTemplate();

	public GooglePayService(Property property, EmailService emailService, MeterRegistry meterRegistry) {
		this.property = property;
		this.emailService = emailService;
		this.meterRegistry = meterRegistry;
		this.recoverCounter = Counter.builder("firebase_service_failures_total")
				.description("Number of failed retries hitting @Recover")
				.register(this.meterRegistry);
	}
}
