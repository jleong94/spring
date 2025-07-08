package com.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

import io.github.bucket4j.Bandwidth;

@Component
public class RateLimitProperties {

	private final Map<String, Bandwidth> limits = new ConcurrentHashMap<>();

	public RateLimitProperties() {
		limits.put("/v1/email/send", Bandwidth.builder()
				.capacity(5)
				.refillGreedy(5, Duration.ofSeconds(60))
				.build());   // 5 req / 60 sec
		limits.put("/v1/email/check", Bandwidth.builder()
				.capacity(2)
				.refillGreedy(2, Duration.ofSeconds(60))
				.build());  // 2 req / 60 sec
	}

    public Bandwidth getLimitForPath(String path) {
        return limits.getOrDefault(path, Bandwidth.builder()
				.capacity(10)
				.refillGreedy(10, Duration.ofSeconds(60))
				.build()); // default
    }
}
