package com.api;

import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pojo.ApiResponse;
import com.pojo.keycloak.User;
import com.service.keycloak.KeycloakService;
import com.utilities.Tool;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class Rest_Auth {
	
	@Autowired
	Tool tool;
	
	@Autowired
	KeycloakService keycloakService;

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
	
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@PostMapping(value = "v1/auth/create", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({User.Create.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	public ResponseEntity<ApiResponse> userCreation(HttpServletRequest request, @RequestBody @Validated({User.Create.class}) User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("mdcId") != null && request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-User creation start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			user = keycloakService.userCreation(log, null, user);
			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.user(user)
					.build());
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
			log.info("-User creation end-");
			MDC.clear();
		}
	}
	
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@PostMapping(value = "v1/auth/maintenance", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({User.Update.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	public ResponseEntity<ApiResponse> userMaintenance(HttpServletRequest request, @RequestBody @Validated({User.Update.class}) User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("mdcId") != null && request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-User maintenance start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			user = keycloakService.userMaintenance(log, null, user, null);
			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.user(user)
					.build());
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
			log.info("-User maintenance end-");
			MDC.clear();
		}
	}
}
