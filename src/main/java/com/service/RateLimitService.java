package com.service;

import com.configuration.RateLimitProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.bucket4j.CustomBucket;

import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.cache.Cache;
import javax.cache.CacheManager;

import java.io.IOException;
import java.util.Map;

@Service
public class RateLimitService {
	
	@Autowired
	private ObjectMapper objectMapper;

	private final Cache<String, CustomBucket> cache;
	
	@Autowired
    private RateLimitProperties rateLimitProperties;

	/**
     * Constructor initializes the rate limit cache using the provided CacheManager.
     *
     * @param cacheManager Spring's CacheManager used to retrieve a named cache.
     */
	public RateLimitService(CacheManager cacheManager) {
		this.cache = cacheManager.getCache("buckets", String.class, CustomBucket.class);
	}
	
	/**
     * Resolves a rate-limiting bucket for a given client key and API path.
     * If the bucket doesn't exist in the cache, it creates and caches a new one
     * with limits defined by the path configuration.
     *
     * @param key  A unique identifier (e.g., IP address or API key) for the caller.
     * @param path The API endpoint being accessed, used to determine the limit.
     * @return The Bucket associated with the given key.
     */
	public CustomBucket resolveBucket(String key, String path) {
		CustomBucket bucket = cache.get(key);// Try to fetch the rate limit bucket from the cache
		if (bucket == null) {
			Bandwidth bandwidth = rateLimitProperties.getLimitForPath(path);
			// Build a new token bucket with the defined bandwidth limit
			bucket = new CustomBucket(bandwidth);
			// Store the newly created bucket in the cache for future use
			cache.put(key, bucket);
		}
		return bucket;
	}

	public String resolveKeyFromRequest(Logger log, HttpServletRequest request, String keyType, String keyValues) throws Exception {
		String result = "";
		try {
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
					resolvedKey = resolveRequestBodyField(log, request, keyValue);
					break;
				default:
					resolvedKey = request.getRequestURI();
				}       
				if (resolvedKey == null || resolvedKey.isBlank()) {
					// If key couldn't be resolved, fall back to IP to prevent null keys
					resolvedKey = getClientIP(request);
					log.warn("Couldn't resolve rate limit key using {}: {}. Falling back to IP: {}", 
							keyType, keyValue, resolvedKey);
				} else {
					result += (result.length() > 0 ? ":" : "") + keyValue + ":" + resolvedKey;
				}
			}
		} catch(Exception e) {
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

	/**
	 * Utility method to extract path variable from the current request.
	 *
	 * @param request      the incoming HTTP request
	 * @param variableName the name of the path variable to resolve (e.g. "userId" in /users/{userId})
	 * @return the value of the path variable if found, otherwise null
	 */
	private String resolvePathVariable(HttpServletRequest request, String variableName) {
		@SuppressWarnings("unchecked")
		Map<String, String> pathVariables = 
		(Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		if (pathVariables != null && pathVariables.containsKey(variableName)) {
			return pathVariables.get(variableName);
		}
		return null;
	}

	/**
	 * Extracts a nested field value from the cached JSON body of an HTTP request.
	 * 
	 * This method relies on Spring's ContentCachingRequestWrapper being present in the request wrapper chain.
	 * It supports dot-notated field paths (e.g., "user.id") to access nested JSON fields.
	 *
	 * @param request    the incoming HttpServletRequest (possibly wrapped)
	 * @param fieldPath  the path to the field in dot notation (e.g. "user.email")
	 * @return the value of the field as a String, or null if not found or unreadable
	 * @throws IOException if reading the body fails
	 */
	private String resolveRequestBodyField(Logger log, HttpServletRequest request, String fieldPath) throws Exception {
		try {
			HttpServletRequest effectiveRequest = request;
			// Traverse wrapper chain to find ContentCachingRequestWrapper
			// This is needed because your request may be wrapped multiple times (e.g., by custom or Spring wrappers)
			while (effectiveRequest instanceof HttpServletRequestWrapper) {
				if (effectiveRequest instanceof ContentCachingRequestWrapper) {
					break;
				}
				effectiveRequest = (HttpServletRequest) ((HttpServletRequestWrapper) effectiveRequest).getRequest();
			}
			// If the caching wrapper isn't present, return null (body can't be re-read safely)
			if (!(effectiveRequest instanceof ContentCachingRequestWrapper)) {
				return null; // Body caching is not available
			}
			// Extract cached body as byte array
			byte[] body = ((ContentCachingRequestWrapper) effectiveRequest).getContentAsByteArray();
			// If body is empty, return null
			if (body.length == 0) return null;
			// Parse the cached body as a JSON tree
			JsonNode rootNode = objectMapper.readTree(body);
			// Traverse nested fields using dot notation (e.g., "user.id" -> root.get("user").get("id"))
			String[] fields = fieldPath.split("\\.");
			JsonNode currentNode = rootNode;
			for (String field : fields) {
				if (currentNode == null) return null;
				currentNode = currentNode.get(field);
			}
			// Return the value as text if found, otherwise null
			return currentNode != null ? currentNode.asText() : null;
		} catch(Exception e) {
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
	}
}
