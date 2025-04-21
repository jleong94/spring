package com.exception;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException.Forbidden;

import com.enums.ResponseCode;
import com.pojo.ApiResponse;
import com.utilities.Tool;

import io.jsonwebtoken.ExpiredJwtException;

/*
 * Here is to handle all the exception that throw by the application
 * */
@RestControllerAdvice
@ControllerAdvice
public class CustomExceptionHandler {
	
	@Autowired
	Tool tool;
	
	@ExceptionHandler({RuntimeException.class, Exception.class})
	public ResponseEntity<ApiResponse> runtimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.ERROR_OCCURED.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}

	@ExceptionHandler({Forbidden.class, AccessDeniedException.class})
	public ResponseEntity<ApiResponse> forbidden(Forbidden e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.UNAUTHORIZED_ACCESS.getResponse_code())
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
				.resp_code(ResponseCode.ERROR_OCCURED.getResponse_code())
				.resp_msg(errorMessage)
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}
	
	@ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse> rateLimitExceededException(RateLimitExceededException e) {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.RATE_LIMIT_EXCEEDED.getResponse_code())
				.resp_msg(e.getMessage())
				.datetime(tool.getTodayDateTimeInString())
				.build());
    }
	
	@ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse> expiredJwtException(ExpiredJwtException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.JWT_TOKEN_EXPIRED.getResponse_code())
				.resp_msg(ResponseCode.JWT_TOKEN_EXPIRED.getResponse_desc())
				.datetime(tool.getTodayDateTimeInString())
				.build());
    }
}
