package com.api;

import java.util.Enumeration;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.configuration.UserInfoDetails;
import com.enums.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.ApiResponse;
import com.pojo.OAuth;
import com.properties.Property;
import com.service.JwtService;
import com.utilities.Tool;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
public class Rest_Auth {

	Logger log = LoggerFactory.getLogger(Rest_Auth.class);
	
	@Autowired
	Property property;
	
	@Autowired
	Tool tool;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	JwtService jwtService;
	
	/**
	 * Logs HTTP request details including headers and parameters
	 * @param request The HTTP request object
	 * @param log Logger instance for logging
	 */
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
	
	/**
	 * OAuth 2.0 token endpoint that handles user authentication and token generation.
	 * 
	 * @param request The HTTP servlet request object
	 * @param ip Client IP address passed as a required header
	 * @param oauth OAuth request body containing username, password and token parameters
	 * @return ResponseEntity with:
	 *         - Status 200 & token details if authentication successful
	 *         - Status 401 if authentication fails
	 *         - Status 500 for server errors
	 * @throws Exception if authentication or token generation fails
	 * 
	 * Flow:
	 * 1. Logs request details and parameters
	 * 2. Authenticates credentials using Spring Security
	 * 3. If authenticated:
	 *    - Sets token expiration (from request or default)
	 *    - Sets token type (from request or default "Bearer")
	 *    - Generates JWT access token
	 *    - Returns success response with token
	 * 4. If not authenticated, returns 401 unauthorized
	 * 5. For any errors, returns 500 with error details
	 */
	@RateLimit
	@PostMapping(value = "v1/oauth-token", consumes = {"application/json"}, produces = "application/json")
	public ResponseEntity<ApiResponse> oauthToken(HttpServletRequest request, @RequestHeader @NotBlank String ip, @RequestBody @Valid OAuth oauth){
		ObjectMapper objectMapper = new ObjectMapper();
		MDC.put("mdcId", UUID.randomUUID());
		log.info("Generate oauth token start...");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(oauth));
			logHttpRequest(request, log);
			
			// Authenticate user credentials using Spring Security
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(oauth.getUsername(), oauth.getPassword()));
			if(authentication.isAuthenticated()) {
				oauth.setExpires_in(property.getJwt_token_expiration());
				oauth.setToken_type(property.getJwt_token_type());
				oauth.setAccess_token(jwtService.generateToken(oauth.getUsername(), (UserInfoDetails) authentication.getPrincipal()));
				return ResponseEntity.status(HttpStatus.OK).body(new com.pojo.ApiResponse(0, tool.getTodayDateTimeInString(log), oauth));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new com.pojo.ApiResponse(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code(), ResponseCode.UNAUTHORIZED_ACCESS.getResponse_desc(), tool.getTodayDateTimeInString(log)));
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
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new com.pojo.ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log)));
		} finally {
			log.info("Generate oauth token end...");
			MDC.clear();
		}
	}
}
