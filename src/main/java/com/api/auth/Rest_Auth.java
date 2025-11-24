package com.api.auth;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.jboss.logging.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.keycloak.AuthRequest;
import com.pojo.keycloak.AuthResponse;
import com.pojo.keycloak.RefreshTokenRequest;
import com.utilities.RequestLoggingUtil;
import com.utilities.Tool;
import com.validation.Audit;
import com.validation.RateLimit;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;

import com.service.keycloak.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j//Only use .info/error for technical/system logs
@RestController
public class Rest_Auth {
	
	private final ObjectMapper objectMapper;

	private final Tool tool;

	private final KeycloakService KeycloakService;
	
	public Rest_Auth(ObjectMapper objectMapper, Tool tool, KeycloakService KeycloakService) {
		this.objectMapper = objectMapper;
		this.tool = tool;
		this.KeycloakService = KeycloakService;
	}
	
	@Operation(
            summary = "Request auth token API",
            description = "To authenticate user using username password & obtain new JWT access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success response")
    })
	@Audit("REQUEST-JWT-API")
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "username")
	@TimeLimiter(name = "requestAuthToken")//To control timeout of endpoint
	@GetMapping(value = "v1/auth/basic", produces = {MediaType.APPLICATION_JSON})
	@JsonView({AuthResponse.Post.class})//Which getter parameter should return within json
	public CompletableFuture<ResponseEntity<com.pojo.ApiResponse>> requestAuthToken(HttpServletRequest request, @RequestBody @Validated({AuthRequest.Post.class}) AuthRequest authRequest) throws Throwable{
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-Request JWT token by username password start-".concat(objectMapper.writeValueAsString(authRequest)));
		try {
			RequestLoggingUtil.logRequestDetails(request, log);
			return CompletableFuture.supplyAsync(() ->
			{
				try {
					return ResponseEntity.status(HttpStatus.FOUND).body(com.pojo.ApiResponse
							.builder()
							.resp_code(ResponseCode.SUCCESS.getResponse_code())
							.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
							.datetime(tool.getTodayDateTimeInString())
							.authResponse(KeycloakService.requestAuthToken(log, authRequest, null))
							.build());
				} catch (Throwable e) {
					throw new CompletionException(e);
				}
			}
					);
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
			log.info("-Request JWT token by username password end-");
			MDC.clear();
		}
	}
	
	@Operation(
            summary = "Request auth token API",
            description = "To authenticate user using refresh token & obtain new JWT access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success response")
    })
	@Audit("REQUEST-JWT-API")
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "refresh_token")
	@TimeLimiter(name = "requestAuthToken")//To control timeout of endpoint
	@GetMapping(value = "v1/auth/token", produces = {MediaType.APPLICATION_JSON})
	@JsonView({AuthResponse.Post.class})//Which getter parameter should return within json
	public CompletableFuture<ResponseEntity<com.pojo.ApiResponse>> requestAuthToken(HttpServletRequest request, @RequestBody @Validated({RefreshTokenRequest.Post.class}) RefreshTokenRequest refreshTokenRequest) throws Throwable{
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-Request JWT token by refresh token start-".concat(objectMapper.writeValueAsString(refreshTokenRequest)));
		try {
			RequestLoggingUtil.logRequestDetails(request, log);
			return CompletableFuture.supplyAsync(() ->
			{
				try {
					return ResponseEntity.status(HttpStatus.FOUND).body(com.pojo.ApiResponse
							.builder()
							.resp_code(ResponseCode.SUCCESS.getResponse_code())
							.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
							.datetime(tool.getTodayDateTimeInString())
							.authResponse(KeycloakService.requestAuthToken(log, null, refreshTokenRequest))
							.build());
				} catch (Throwable e) {
					throw new CompletionException(e);
				}
			}
					);
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
			log.info("-Request JWT token by refresh token end-");
			MDC.clear();
		}
	}
}
