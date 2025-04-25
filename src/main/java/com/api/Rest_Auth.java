package com.api;

import java.util.Enumeration;
import java.util.UUID;

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

import com.configuration.UserInfoDetails;
import com.enums.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modal.User;
import com.pojo.ApiResponse;
import com.pojo.OAuth;
import com.properties.Property;
import com.service.JwtService;
import com.service.UserService;
import com.utilities.Tool;
import com.validation.RateLimit;
import com.validation.UserValidationGroups;

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
					log.info(headerName + ": " + new String(request.getHeader(headerName).getBytes("ISO-8859-1"), "UTF-8"));
				}
			}
			Enumeration<String> parameterNames = request.getParameterNames();
			if(parameterNames != null) {
				while(parameterNames.hasMoreElements()) {
					String parameterName = parameterNames.nextElement();
					log.info(parameterName + ": " + new String(request.getParameter(parameterName).getBytes("ISO-8859-1"), "UTF-8"));
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
	
	@RateLimit
	@PostMapping(value = "v1/oauth-token", consumes = {"application/json"}, produces = "application/json")
	public ResponseEntity<ApiResponse> oauthToken(HttpServletRequest request, @RequestBody @Validated OAuth oauth) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();
		MDC.put("mdcId", UUID.randomUUID());
		log.info("-Generate oauth token start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(oauth));
			logHttpRequest(request, log);
			
			// Authenticate user credentials using Spring Security
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(oauth.getUsername(), oauth.getPassword()));
			if(authentication.isAuthenticated()) {
				UserInfoDetails userInfoDetails = (UserInfoDetails) authentication.getPrincipal();
				oauth.setToken_type(property.getJwt_token_type());
				oauth.setAccess_token(jwtService.generateToken(oauth.getUsername(), userInfoDetails));
				return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
						.builder()
						.resp_code(ResponseCode.SUCCESS.getResponse_code())
						.resp_msg(ResponseCode.SUCCESS.getResponse_status())
						.datetime(tool.getTodayDateTimeInString())
						.oauth(oauth)
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
	
	@RateLimit
	@PostMapping(value = "v1/user/registration", consumes = {"application/json"}, produces = "application/json")
	public ResponseEntity<ApiResponse> userRegistration(HttpServletRequest request, @RequestBody @Validated({UserValidationGroups.Create.class}) User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();
		MDC.put("mdcId", UUID.randomUUID());
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
	
	@RateLimit
	@PutMapping(value = "v1/reset/password", consumes = {"application/json"}, produces = "application/json")
	public ResponseEntity<ApiResponse> resetPassword(HttpServletRequest request, @RequestBody @Validated({UserValidationGroups.Update.class}) User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();
		MDC.put("mdcId", UUID.randomUUID());
		log.info("-Reset password start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			return null;
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
