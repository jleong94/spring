package com.api;

import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modal.User;
import com.pojo.ApiResponse;
import com.pojo.UserInfoDetails;
import com.properties.Property;
import com.service.JwtService;
import com.service.UserService;
import com.utilities.Tool;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class Rest_Auth {
	
	@Autowired
	Property property;
	
	@Autowired
	Tool tool;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	JwtService jwtService;
	
	@Autowired
	UserService userService;

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
	
	@RateLimit(capacity = 10, tokens = 10, period = 60)
	@PostMapping(value = "v1/oauth-token", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({User.OauthToken.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	public ResponseEntity<ApiResponse> oauthToken(HttpServletRequest request, @RequestBody @Validated({User.OauthToken.class}) User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("X-Correlation-ID") != null ? request.getHeader("X-Correlation-ID") : UUID.randomUUID());
		log.info("-Generate oauth token start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			// Authenticate user credentials using Spring Security
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
			if(authentication.isAuthenticated()) {
				UserInfoDetails userInfoDetails = (UserInfoDetails) authentication.getPrincipal();
				user.setToken_type(property.getJwt_token_type());
				user.setAccess_token(jwtService.generateToken(user.getUsername(), userInfoDetails));
				return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
						.builder()
						.resp_code(ResponseCode.SUCCESS.getResponse_code())
						.resp_msg(ResponseCode.SUCCESS.getResponse_status())
						.datetime(tool.getTodayDateTimeInString())
						.user(user)
						.build());
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
						.builder()
						.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
						.resp_msg(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_desc())
						.datetime(tool.getTodayDateTimeInString())
						.build());
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
		} finally {
			log.info("-Generate oauth token end-");
			MDC.clear();
		}
	}
	
	@RateLimit(capacity = 10, tokens = 10, period = 60)
	@PostMapping(value = "v1/user/registration", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({User.UserRegistration.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	public ResponseEntity<ApiResponse> userRegistration(HttpServletRequest request, @RequestBody @Validated({User.UserRegistration.class}) User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("X-Correlation-ID") != null ? request.getHeader("X-Correlation-ID") : UUID.randomUUID());
		log.info("-User registration start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			return userService.userRegistration(user);
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
			log.info("-User registration end-");
			MDC.clear();
		}
	}
	
	@RateLimit(capacity = 10, tokens = 10, period = 60)
	@PutMapping(value = "v1/reset/password", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({User.ResetPassword.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	public ResponseEntity<ApiResponse> resetPassword(HttpServletRequest request, @RequestBody @Validated({User.ResetPassword.class}) User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("X-Correlation-ID") != null ? request.getHeader("X-Correlation-ID") : UUID.randomUUID());
		log.info("-Reset password start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			return userService.resetPassword(user);
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
			log.info("-Reset password end-");
			MDC.clear();
		}
	}
}
