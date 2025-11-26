package com.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.service.AuthService;
import com.utilities.RequestLoggingUtil;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/*
 * This class will call in class, SecurityConfig to perform authentication & authorization on JWT token receive
 * */
@Slf4j
@Component
public class CustomOncePerRequestFilter extends OncePerRequestFilter {

	private final AuthService authService;

	public CustomOncePerRequestFilter(AuthService authService) {
		this.authService = authService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException{		
		try {
			UUID xRequestId = UUID.randomUUID();

			// Wrap request to add custom header
			ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request) {
				@Override
				public String getHeader(String name) {
					if ("X-Request-ID".equals(name)) {
						return xRequestId.toString();
					}
					return super.getHeader(name);
				}

				@Override
				public Enumeration<String> getHeaderNames() {
					Set<String> headerNames = new HashSet<>();
					Enumeration<String> originalHeaders = super.getHeaderNames();
					while (originalHeaders.hasMoreElements()) {
						headerNames.add(originalHeaders.nextElement());
					}
					headerNames.add("X-Request-ID");
					return Collections.enumeration(headerNames);
				}
			};

			// Also add to response for client tracking
			response.setHeader("X-Request-ID", xRequestId.toString());

			MDC.put("X-Request-ID", response.getHeader("X-Request-ID") != null && !response.getHeader("X-Request-ID").isBlank() ? response.getHeader("X-Request-ID") : UUID.randomUUID());
			log.info("-Custom once per request filter start-");
			RequestLoggingUtil.logRequestDetails(wrappedRequest, log);

			String signature = wrappedRequest.getHeader("SIGNATURE");
			if(signature != null) {
				String requestBody = getRequestBody(wrappedRequest);
				Authentication authentication = authService.isSignatureValid(log, wrappedRequest.getRequestURI(), requestBody, signature);
				if(authentication.isAuthenticated()) {SecurityContextHolder.getContext().setAuthentication(authentication);}
			}

			chain.doFilter(wrappedRequest, response);
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
			throw e;
		} finally {
			log.info("-Custom once per request filter end-");
			MDC.clear();
		}
	}

	private String getRequestBody(ContentCachingRequestWrapper request) throws IOException {
		// Method 1: Read input stream directly (this triggers caching)
		@SuppressWarnings("unused")
		byte[] content = StreamUtils.copyToByteArray(request.getInputStream());

		// Now getContentAsByteArray() will work
		byte[] cachedContent = request.getContentAsByteArray();

		return cachedContent.length > 0 ? 
				new String(cachedContent, StandardCharsets.UTF_8) : "";
	}
}
