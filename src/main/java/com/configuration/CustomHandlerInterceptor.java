package com.configuration;

import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.exception.RateLimitExceededException;
import com.pojo.bucket4j.CustomBucket;
import com.service.RateLimitService;
import com.validation.RateLimit;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomHandlerInterceptor implements HandlerInterceptor {

	private final RateLimitService rateLimitService;
	
	public CustomHandlerInterceptor(RateLimitService rateLimitService) {
		this.rateLimitService = rateLimitService;
	}
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-Handler interceptor preHandle start-");
		try {
			
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
		} catch(Throwable e) {
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
			throw new Exception(e);
		} finally {
			log.info("-Handler interceptor preHandle end-");
			MDC.clear();
		}
    }
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-Handler interceptor afterCompletion start-");
		try {
			if (request.getDispatcherType() != DispatcherType.REQUEST || request.getAttribute("CustomHandlerInterceptor") != null) {
				return; //Avoid same full logic run twice for handler interceptor
			}
			request.setAttribute("CustomHandlerInterceptor", Boolean.TRUE);
			// Check if the handler is a HandlerMethod (i.e., a controller method).
			// This allows access to method-level annotations such as @RateLimitHeader.
			if (!(handler instanceof HandlerMethod)) {
				return;
			}
			// Cast the generic handler object to HandlerMethod to access controller method metadata
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			// Retrieve the @RateLimit annotation (custom) from the handler method, if present
			RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

			String resolvedIpKey = rateLimitService.resolveKeyFromRequest(log, request, "ip", "");
			String headerName = rateLimit.headerName();
			String resolvedHeaderKey = headerName != null && !headerName.isBlank() ? rateLimitService.resolveKeyFromRequest(log, request, "header", headerName) : null;
			String pathVariable = rateLimit.pathVariable();
			String resolvedPathVariableKey = pathVariable != null && !pathVariable.isBlank() ? rateLimitService.resolveKeyFromRequest(log, request, "pathVariable", pathVariable) : null;
			String requestBodyField = rateLimit.requestBodyField();
			String resolvedRequestBodyFieldKey = requestBodyField != null && !requestBodyField.isBlank() ? rateLimitService.resolveKeyFromRequest(log, request, "requestBody", requestBodyField) : null;

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
			log.info("Resolved key {}", resolvedKey);
			CustomBucket bucket = rateLimitService.resolveBucket(resolvedKey, request.getRequestURI());
			boolean allowed = bucket.tryConsume(1);

			if (!allowed) {
				log.info("Rate limit exceeded for key: {} at endpoint {} with available tokens: {}", resolvedKey, request.getRequestURI(), bucket.getAvailableTokens());
				throw new RateLimitExceededException("Rate limit exceeded");
			}

			return;
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
		} catch(Throwable e) {
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
			throw new Exception(e);
		} finally {
			log.info("-Handler interceptor afterCompletion end-");
			MDC.clear();
		}
	}
}
