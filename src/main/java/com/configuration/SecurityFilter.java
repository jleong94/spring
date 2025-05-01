package com.configuration;

import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import com.pojo.UserInfoDetails;
import com.service.JwtService;
import com.service.UserService;

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
public class SecurityFilter extends OncePerRequestFilter {
	
	@Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;
	
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
		}
	}
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException{
		MDC.put("mdcId", UUID.randomUUID());
		log.info("-Security filter start-");
		try {
			logHttpRequest(request, log);
			String token = request.getHeader("Authorization");
			if (token == null || !token.startsWith("Bearer ")) {
				chain.doFilter(request, response);
				return;
			}
			if(token != null && token.startsWith("Bearer ")) {
				token = token.replace("Bearer ", "").trim();
				if (SecurityContextHolder.getContext().getAuthentication() == null) {
					String username = jwtService.extractUsername(token);
					if(username != null && !username.isBlank()) {
						UserInfoDetails userInfoDetails = (UserInfoDetails) userService.loadUserByUsername(username);
						if (jwtService.validateToken(token, userInfoDetails.getJwt_token_secret_key())) {
							UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userInfoDetails, null, userInfoDetails.getAuthorities());
							usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
							SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
						}
					}		            
				}
			}
			chain.doFilter(request, response);
		} finally {
			log.info("-Security filter end-");
			MDC.clear();
		}
	}
}
