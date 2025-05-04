package com.configuration;

import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.exception.RateLimitExceededException;
import com.service.RateLimitService;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

	@Autowired
	private RateLimitService rateLimitService;
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		MDC.put("mdcId", request.getHeader("X-Correlation-ID") != null ? request.getHeader("X-Correlation-ID") : UUID.randomUUID());
		log.info("-Rate limit interceptor start-");
		try {
			if (!(handler instanceof HandlerMethod)) {
				return true;
			}

			HandlerMethod handlerMethod = (HandlerMethod) handler;
			RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

			if (rateLimit == null) {
				// No rate limit annotation, proceed as normal
				return true;
			}

			String ip = rateLimit.ip();
			String resolvedIpKey = rateLimitService.resolveKeyFromRequest(log, request, "ip", ip);
			String headerName = rateLimit.headerName();
			String resolvedHeaderKey = rateLimitService.resolveKeyFromRequest(log, request, "header", headerName);
			String pathVariable = rateLimit.pathVariable();
			String resolvedPathVariableKey = rateLimitService.resolveKeyFromRequest(log, request, "pathVariable", pathVariable); 
			String requestBodyField = rateLimit.requestBodyField();
			String resolvedRequestBodyFieldKey = rateLimitService.resolveKeyFromRequest(log, request, "requestBody", requestBodyField); 
			int capacity = rateLimit.capacity(), tokens = rateLimit.tokens(), period = rateLimit.period();

			// Resolve the actual key from the request
			String resolvedKey = resolvedIpKey;
			if(resolvedHeaderKey != null && !resolvedHeaderKey.isBlank()) {
				if(resolvedKey != null && !resolvedKey.isBlank()) {
					resolvedKey = resolvedKey.concat("|");
				}
				resolvedKey = resolvedKey.concat(resolvedHeaderKey);
			} if(resolvedPathVariableKey != null && !resolvedPathVariableKey.isBlank()) {
				if(resolvedKey != null && !resolvedKey.isBlank()) {
					resolvedKey = resolvedKey.concat("|");
				}
				resolvedKey = resolvedKey.concat(resolvedPathVariableKey);
			} if(resolvedRequestBodyFieldKey != null && !resolvedRequestBodyFieldKey.isBlank()) {
				if(resolvedKey != null && !resolvedKey.isBlank()) {
					resolvedKey = resolvedKey.concat("|");
				}
				resolvedKey = resolvedKey.concat(resolvedRequestBodyFieldKey);
			}
			// Try to consume a token
			boolean allowed = rateLimitService.tryConsume(resolvedKey, capacity, tokens, period);

			if (!allowed) {
				long availableTokens = rateLimitService.getAvailableTokens(resolvedKey, capacity, tokens, period);
				log.info("Rate limit exceeded for key: {}, available tokens: {}", resolvedKey, availableTokens);
				throw new RateLimitExceededException("Rate limit exceeded");
			}

			return true;
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
		} finally {
			log.info("-Rate limit interceptor end-");
			MDC.clear();
		}
    }
}
