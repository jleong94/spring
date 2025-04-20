package com.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException.Forbidden;

import com.enums.ResponseCode;
import com.pojo.ApiResponse;
import com.utilities.Tool;

/*
 * Here is to handle all the exception that throw by the application
 * */
@RestControllerAdvice
@ControllerAdvice
public class CustomExceptionHandler {
	
	@Autowired
	Tool tool;
	
	Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse> runtimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse(ResponseCode.CATCHED_EXCEPTION.getResponse_code(), e.getMessage(), tool.getTodayDateTimeInString(log)));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse> accessDeniedException(AccessDeniedException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse(ResponseCode.CATCHED_EXCEPTION.getResponse_code(), "Access denied.", tool.getTodayDateTimeInString(log)));
	}

	@ExceptionHandler(Forbidden.class)
	public ResponseEntity<ApiResponse> forbidden(Forbidden e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ApiResponse(ResponseCode.CATCHED_EXCEPTION.getResponse_code(), "Unauthrized access.", tool.getTodayDateTimeInString(log)));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> exception(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				new ApiResponse(ResponseCode.ERROR_OCCURED.getResponse_code(), ResponseCode.ERROR_OCCURED.getResponse_desc(), tool.getTodayDateTimeInString(log)));
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		String errorMsg = e.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.findFirst()
				.orElse("Validation failed");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse(ResponseCode.CATCHED_EXCEPTION.getResponse_code(), errorMsg, tool.getTodayDateTimeInString(log)));
	}
	
	@ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse> handleRateLimitExceeded(RateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiResponse(
                    ResponseCode.RATE_LIMIT_EXCEEDED.getResponse_code(),
                    e.getMessage(),
                    tool.getTodayDateTimeInString(log)));
    }
}
