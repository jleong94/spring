package com.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A configuration component that holds rate limit settings (bandwidth rules) for different API paths.
 * 
 * Each path is mapped to a {@link Bandwidth} configuration, which defines the number of allowed requests
 * and how tokens are refilled over time.
 * 
 * Uses Bucket4j's Bandwidth to implement token bucket rate limiting per endpoint.
 */
@Configuration
@ConfigurationProperties(prefix = "rate.limit")
@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
public class RateLimitProperties {
	
	private Rate defaultRate;
	
	@Builder.Default
    protected Map<String, Rate> endpoints = new ConcurrentHashMap<>();

	// A thread-safe map holding path-specific rate limit rules
	private final Map<String, Bandwidth> limits = new ConcurrentHashMap<>();

	@PostConstruct
    public void init() {
		// Default
	    limits.put("default", Bandwidth.builder()
	            .capacity(defaultRate.getCapacity())
	            .refillGreedy(defaultRate.getTokens(), Duration.ofSeconds(defaultRate.getPeriod()))
	            .build());

	    // Endpoints
        endpoints.forEach((path, rate) -> 
            limits.put(path, Bandwidth.builder()
                    .capacity(rate.getCapacity())
                    .refillGreedy(rate.getTokens(), Duration.ofSeconds(rate.getPeriod()))
                    .build())
        );
    }

	/**
     * Retrieves the {@link Bandwidth} limit configuration for a specific API path.
     * If no specific limit is defined for the path, a default limit is applied.
     *
     * @param path the API endpoint path
     * @return the Bandwidth configuration for the given path
     */
    public Bandwidth getLimitForPath(String path) {
        return limits.getOrDefault(path, limits.get("default")); // default
    }
    
    @Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
    @Builder(toBuilder = true)
    public static class Rate {
    	
        private int capacity;
    	
        private int tokens;
        
        private int period;
    }
}
