package com.service;

import com.configuration.RateLimitProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.cache.Cache;
import javax.cache.CacheManager;

import java.io.IOException;
import java.util.Map;

@Service
public class RateLimitService {
	
	@Autowired
	private ObjectMapper objectMapper;

	private final Cache<String, Bucket> cache;
	
	@Autowired
    private RateLimitProperties rateLimitProperties;

	public RateLimitService(CacheManager cacheManager) {
		this.cache = cacheManager.getCache("buckets", String.class, Bucket.class);
	}
	
	public Bucket resolveBucket(String key, String path) {
        Bucket bucket = cache.get(key);
        if (bucket == null) {
            Bandwidth bandwidth = rateLimitProperties.getLimitForPath(path);
            bucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();
            cache.put(key, bucket);
        }
        return bucket;
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
