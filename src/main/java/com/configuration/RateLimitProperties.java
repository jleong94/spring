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
				.capacity(50)//maximum number of tokens (or requests) allowed in the bucket
				.refillGreedy(5, Duration.ofSeconds(5))//Every nth seconds, instantly add nth tokens back
				.build());   
		limits.put("/v1/email/check", Bandwidth.builder()
				.capacity(100)//maximum number of tokens (or requests) allowed in the bucket
				.refillGreedy(5, Duration.ofSeconds(3))//Every nth seconds, instantly add nth tokens back
				.build());  
	}

    public Bandwidth getLimitForPath(String path) {
        return limits.getOrDefault(path, Bandwidth.builder()
				.capacity(10)
				.refillGreedy(1, Duration.ofSeconds(1))
				.build()); // default
    }
}
