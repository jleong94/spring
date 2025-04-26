package com.exception;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.enums.ResponseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.ApiResponse;
import com.utilities.Tool;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

/*
 * Here is to handle all the exception that throw by the application
 * */
@Slf4j
@RestControllerAdvice
@ControllerAdvice
public class CustomExceptionHandler implements ResponseBodyAdvice<Object> {

	@Autowired
	Tool tool;

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		// Apply to all responses
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body,
			MethodParameter returnType,
			MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType,
					ServerHttpRequest request,
					ServerHttpResponse response) {
		// Log method, URL, and response body
		try {
			log.info("Response to [{} {}] => {}", 
					request.getMethod(), 
					request.getURI(), 
					new ObjectMapper().writeValueAsString(body));
		} catch (JsonProcessingException e) {
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
		return body; // Don’t modify the response, just log it
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse> runtimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> exception(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		String errorMessage = e.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", ")); // Combine all messages into a single string
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR.getResponse_code())
				.resp_msg(errorMessage)
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<ApiResponse> rateLimitExceededException(RateLimitExceededException e) {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(MessagingException.class)
	public ResponseEntity<ApiResponse> messagingException(MessagingException e) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(UnsupportedOperationException.class)
	public ResponseEntity<ApiResponse> unsupportedOperationException(UnsupportedOperationException e) {
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ApiResponse> validationException(ValidationException e) {
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ApiResponse> usernameNotFoundException(UsernameNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse> badCredentialsException(BadCredentialsException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(AccountExpiredException.class)
	public ResponseEntity<ApiResponse> accountExpiredException(AccountExpiredException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(LockedException.class)
	public ResponseEntity<ApiResponse> lockedException(LockedException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ApiResponse> disabledException(DisabledException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(CredentialsExpiredException.class)
	public ResponseEntity<ApiResponse> credentialsExpiredException(CredentialsExpiredException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
	public ResponseEntity<ApiResponse> authenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse> accessDeniedException(AccessDeniedException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(InsufficientAuthenticationException.class)
	public ResponseEntity<ApiResponse> insufficientAuthenticationException(InsufficientAuthenticationException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler(JwtException.class)
	public ResponseEntity<ApiResponse> jwtException(JwtException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}
}
