package com.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.properties.Property;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.cache.Cache;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Service
public class RateLimitService {

	@Autowired
	private Property property;
	
	private final ProxyManager<String> buckets;
	
	@Autowired
	private ObjectMapper objectMapper;

	public RateLimitService(Cache<String, byte[]> cache) {
		this.buckets = new JCacheProxyManager<>(cache);
	}

	public boolean tryConsume(String key, int capacity, int tokens, int period) {
		Bucket bucket = resolveBucket(key, capacity, tokens, period);
		return bucket.tryConsume(1);
	}

	public long getAvailableTokens(String key, int capacity, int tokens, int period) {
		Bucket bucket = resolveBucket(key, capacity, tokens, period);
		return bucket.getAvailableTokens();
	}

	private Bucket resolveBucket(String key, int capacity, int tokens, int period) {
		return buckets.builder().build(key, () -> getBucketConfiguration(capacity, tokens, period));
	}

	private BucketConfiguration getBucketConfiguration(int capacity, int tokens, int period) {
		capacity = capacity > 0 ? capacity : property.getRate_limit_capacity();
		tokens = tokens > 0 ? tokens : property.getRate_limit_tokens();
		period = period > 0 ? period : property.getRate_limit_period();
		Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.greedy(tokens, Duration.ofSeconds(period)));
		return BucketConfiguration.builder().addLimit(bandwidth).build();
	}

	public String resolveKeyFromRequest(Logger log, HttpServletRequest request, String keyType, String keyValues) throws IOException {
		String result = "";
		for(String keyValue : keyValues.split(",")) {
			String resolvedKey = "";
			switch (keyType) {
			case "ip":
				resolvedKey = getClientIP(request);
				break;
			case "header":
				resolvedKey = request.getHeader(keyValue);
				break;
			case "pathVariable":
				resolvedKey = resolvePathVariable(request, keyValue);
				break;
			case "requestBody":
				resolvedKey = resolveRequestBodyField(request, keyValue);
				break;
			default:
				resolvedKey = request.getRequestURI();
			}       
			if (resolvedKey == null || resolvedKey.isBlank()) {
				// If key couldn't be resolved, fall back to IP to prevent null keys
				resolvedKey = getClientIP(request);
				log.warn("Couldn't resolve rate limit key using {}: {}. Falling back to IP: {}", 
						keyType, keyValue, resolvedKey);
			} else {result = keyValue + ":" + resolvedKey;}
		}
		// Prepend the keyType to ensure uniqueness across different key types
		return result;
	}

	private String getClientIP(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			// Get the first IP which is the client's IP
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private String resolvePathVariable(HttpServletRequest request, String variableName) {
		@SuppressWarnings("unchecked")
		Map<String, String> pathVariables = 
		(Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		if (pathVariables != null && pathVariables.containsKey(variableName)) {
			return pathVariables.get(variableName);
		}
		return null;
	}

	private String resolveRequestBodyField(HttpServletRequest request, String fieldPath) throws IOException {
		// This is a simplified approach - in production you might want to use
		// request wrappers to allow reading the body multiple times
		JsonNode rootNode = objectMapper.readTree(request.getInputStream());

		// Support for nested fields using dot notation (e.g., "user.id")
		String[] fields = fieldPath.split("\\.");
		JsonNode currentNode = rootNode;

		for (String field : fields) {
			if (currentNode == null) return null;
			currentNode = currentNode.get(field);
		}

		return currentNode != null ? currentNode.asText() : null;
	}
}
