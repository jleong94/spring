package com.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/*
 * This class will call in class, SecurityConfig to perform authentication & authorization on JWT token receive
 * */
@Slf4j
@Component
public class CustomOncePerRequestFilter extends OncePerRequestFilter {
	
	private void logHttpRequest(HttpServletRequest request, Logger log) {
		try {
			Enumeration<String> headerNames = request.getHeaderNames();
			if(headerNames != null) {
				while(headerNames.hasMoreElements()) {
					String headerName = headerNames.nextElement();
					log.info(headerName + ": " + StringEscapeUtils.escapeHtml4(request.getHeader(headerName)));
				}
			}
			Enumeration<String> parameterNames = request.getParameterNames();
			if(parameterNames != null) {
				while(parameterNames.hasMoreElements()) {
					String parameterName = parameterNames.nextElement();
					log.info(parameterName + ": " + StringEscapeUtils.escapeHtml4(request.getParameter(parameterName)));
				}
			}
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
		}
	}
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException{		
		try {
			UUID xRequestId = UUID.randomUUID();
			
			// Wrap request to add custom header
	        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
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
			logHttpRequest(request, log);

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
}
