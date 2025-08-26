package com.api;

import java.time.Duration;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pojo.ApiResponse;
import com.pojo.Jwt;
import com.pojo.keycloak.User;
import com.service.RateLimitService;
import com.service.keycloak.KeycloakService;
import com.utilities.Tool;
import com.validation.RateLimit;

import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class Rest_Auth {
	
	@Autowired
	Tool tool;
	
	@Autowired
	KeycloakService keycloakService;
	
	@Autowired
	RateLimitService rateLimitService;

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
	@PostMapping(value = "v1/auth/signup", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({User.Create.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	@Validated({User.Create.class})
	public ResponseEntity<ApiResponse> userCreation(HttpServletRequest request, @RequestBody User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-User creation start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			user = keycloakService.userCreation(log, user);
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
	@PutMapping(value = "v1/auth/maintenance", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({User.Update.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	@Validated({User.Update.class}) 
	public ResponseEntity<ApiResponse> userMaintenance(HttpServletRequest request, @RequestBody User user) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-User maintenance start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(user));
			logHttpRequest(request, log);
			
			user = keycloakService.userMaintenance(log, user);
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
	

	
	@RateLimit(headerName = "", pathVariable = "username", requestBodyField = "")
	@GetMapping(value = "v1/auth/check/{username}", produces = "application/json; charset=UTF-8")
	@JsonView({User.Select.class})//Which getter parameter should return within json
	@Validated({}) 
	public ResponseEntity<ApiResponse> getUserDetailByUsername(HttpServletRequest request, @PathVariable @NotBlank String username) throws Exception{
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-Get user detail start-");
		try {
			logHttpRequest(request, log);
			
			User user = User.builder()
					.username(username)
					.build();
			user = keycloakService.getUserDetailByUsernameOrId(log, user);
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
			log.info("-Get user detail end-");
			MDC.clear();
		}
	}
	
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@PostMapping(value = "v1/request-jwt", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	@Validated({}) 
	public ResponseEntity<ApiResponse> requestJwt(HttpServletRequest request, @RequestBody Jwt jwt) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-Request jwt start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(jwt));
			logHttpRequest(request, log);
			if ("password".equalsIgnoreCase(jwt.getGrant_type())) {
				jwt = keycloakService.requestAccessTokenViaPassword(log, jwt);
			} else if("refresh_token".equalsIgnoreCase(jwt.getGrant_type())) {
				jwt = keycloakService.requestAccessTokenViaRefreshToken(log, jwt);
			}
			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.jwt(jwt)
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
			log.info("-Request jwt end-");
			MDC.clear();
		}
	}
	
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@PostMapping(value = "v1/rate-limits/update", consumes = {"application/x-www-form-urlencoded; charset=UTF-8", "multipart/form-data; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	@Validated({}) 
	public ResponseEntity<ApiResponse> updRateLimit(HttpServletRequest request, @RequestParam String key, @RequestParam @NotBlank(message = "Path is blank.") String path, @RequestParam @Min(1) long capacity, @RequestParam @Min(1) long refillTokens, @RequestParam @Min(1) long refillSeconds) throws Exception{
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-Rate limit update start-");
		try {
			logHttpRequest(request, log);
			Bandwidth bandwidth = Bandwidth.builder()
					.capacity(capacity)//maximum number of tokens (or requests) allowed in the bucket
					.refillGreedy(refillTokens, Duration.ofSeconds(refillSeconds))//Every nth seconds, instantly add nth tokens back
					.build();
			rateLimitService.updateBucketLimit(key, path, bandwidth);
			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
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
			log.info("-Rate limit update end-");
			MDC.clear();
		}
	}
}
