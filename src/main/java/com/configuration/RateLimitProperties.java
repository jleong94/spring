package com.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

import io.github.bucket4j.Bandwidth;

/**
 * A configuration component that holds rate limit settings (bandwidth rules) for different API paths.
 * 
 * Each path is mapped to a {@link Bandwidth} configuration, which defines the number of allowed requests
 * and how tokens are refilled over time.
 * 
 * Uses Bucket4j's Bandwidth to implement token bucket rate limiting per endpoint.
 */
@Component
public class RateLimitProperties {

	// A thread-safe map holding path-specific rate limit rules
	private final Map<String, Bandwidth> limits = new ConcurrentHashMap<>();

	/**
     * Initializes the rate limit configuration for specific endpoints.
     * Additional endpoints can be added here with their own rate limit rules.
     */
	public RateLimitProperties() {
		limits.put("/v1/template/post", Bandwidth.builder()
				.capacity(10)//maximum number of tokens (or requests) allowed in the bucket
				.refillGreedy(5, Duration.ofSeconds(1 * 60))//Every nth seconds, instantly add nth tokens back
				.build());
		limits.put("/v1/template/get", Bandwidth.builder()
				.capacity(10)//maximum number of tokens (or requests) allowed in the bucket
				.refillGreedy(5, Duration.ofSeconds(1 * 60))//Every nth seconds, instantly add nth tokens back
				.build());  
		limits.put("/v1/template/put", Bandwidth.builder()
				.capacity(10)//maximum number of tokens (or requests) allowed in the bucket
				.refillGreedy(5, Duration.ofSeconds(1 * 60))//Every nth seconds, instantly add nth tokens back
				.build());  
		limits.put("/v1/template/delete", Bandwidth.builder()
				.capacity(10)//maximum number of tokens (or requests) allowed in the bucket
				.refillGreedy(5, Duration.ofSeconds(1 * 60))//Every nth seconds, instantly add nth tokens back
				.build());    
	}

	/**
     * Retrieves the {@link Bandwidth} limit configuration for a specific API path.
     * If no specific limit is defined for the path, a default limit is applied.
     *
     * @param path the API endpoint path
     * @return the Bandwidth configuration for the given path
     */
    public Bandwidth getLimitForPath(String path) {
        return limits.getOrDefault(path, Bandwidth.builder()
				.capacity(10)
				.refillGreedy(1, Duration.ofSeconds(1 * 60))
				.build()); // default
    }
}
