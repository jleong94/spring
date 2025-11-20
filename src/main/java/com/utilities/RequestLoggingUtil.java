package com.utilities;

import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * Utility class for logging HTTP request headers and parameters in an enterprise-grade manner.
 * Implements security best practices including sensitive data masking and structured logging.
 */
public class RequestLoggingUtil {

	// Sensitive headers/parameters that should be masked in logs
	private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
			"authorization", "username", "password", "client-id", "client-secret", "api-key",
			"access-token", "refresh-token", "session", "cookie", "x-api-key", "card_no", "cardno", "cvv", "cvc"
			));

	private static final String MASK_VALUE = "***REDACTED***";
	private static final int MAX_PARAM_LENGTH = 1000; // Prevent log flooding

	/**
	 * Logs all headers and parameters from the HTTP request with sensitive data masking.
	 *
	 * @param request the HttpServletRequest to log
	 */
	public static void logRequestDetails(HttpServletRequest request, Logger log) {
		if (request == null) {
			log.warn("Attempted to log null HttpServletRequest");
			return;
		}

		log.info("Request Details - Method: {}, URI: {}, RemoteAddr: {}",
				request.getMethod(),
				request.getRequestURI(),
				request.getRemoteAddr()
				);

		logHeaders(request, log);
		logParameters(request, log);
	}

	/**
	 * Logs all HTTP headers with sensitive data masking.
	 *
	 * @param request the HttpServletRequest
	 * @param requestId unique identifier for the request
	 */
	private static void logHeaders(HttpServletRequest request, Logger log) {
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				String headerValue = request.getHeader(headerName);

				// Mask sensitive headers
				if (isSensitive(headerName)) {
					log.info("Request Headers{}: {}", headerName, MASK_VALUE);
				} else {
					log.info("Request Headers{}: {}", headerName, truncate(headerValue, MAX_PARAM_LENGTH));
				}
			}
		} else {
			log.debug("No headers found.");
		}
	}

	/**
	 * Logs all HTTP parameters with sensitive data masking.
	 *
	 * @param request the HttpServletRequest
	 * @param requestId unique identifier for the request
	 */
	private static void logParameters(HttpServletRequest request, Logger log) {
		Map<String, String[]> parameterMap = request.getParameterMap();

		if (parameterMap != null && !parameterMap.isEmpty()) {
			parameterMap.forEach((key, values) -> {
				if (isSensitive(key)) {
					log.info("Request Parameters{}: {}", key, MASK_VALUE);
				} else {
					String value = values.length == 1 
							? truncate(values[0], MAX_PARAM_LENGTH)
									: Arrays.toString(values);
					log.info("Request Parameters{}: {}", key, truncate(value, MAX_PARAM_LENGTH));
				}
			});
		} else {
			log.debug("No parameters found.");
		}
	}

	/**
	 * Checks if a key (header or parameter name) is sensitive and should be masked.
	 *
	 * @param key the key to check
	 * @return true if the key is sensitive
	 */
	private static boolean isSensitive(String key) {
		if (!StringUtils.hasText(key)) {
			return false;
		}

		String lowerKey = key.toLowerCase();
		return SENSITIVE_KEYS.stream().anyMatch(lowerKey::contains);
	}

	/**
	 * Truncates a string to a maximum length to prevent log flooding.
	 *
	 * @param value the value to truncate
	 * @param maxLength maximum allowed length
	 * @return truncated string
	 */
	private static String truncate(String value, int maxLength) {
		if (value == null) {
			return null;
		}

		if (value.length() <= maxLength) {
			return value;
		}

		return value.substring(0, maxLength) + "... [TRUNCATED]";
	}
}
