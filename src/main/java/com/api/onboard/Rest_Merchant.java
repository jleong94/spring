package com.api.onboard;

import java.util.Enumeration;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modal.onboard.Merchant;
import com.pojo.ApiResponse;
import com.properties.Property;
import com.service.onboard.MerchantService;
import com.utilities.Tool;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
public class Rest_Merchant {

	Logger log = LoggerFactory.getLogger(Rest_Merchant.class);
	
	@Autowired
	Property property;
	
	@Autowired
	Tool tool;
	
	@Autowired
	MerchantService merchantService;
	
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
	@PostMapping(value = "v1/merchant/create", consumes = {"application/json"}, produces = "application/json")
	@PreAuthorize("hasAnyRole('ROLE_User', 'ROLE_Admin') and hasAnyAuthority('Merchant onboarding & maintenance_write')")
	public ResponseEntity<ApiResponse> createMerchant(HttpServletRequest request, @RequestHeader @NotBlank String ip, @RequestBody @Valid Merchant merchant){
		ObjectMapper objectMapper = new ObjectMapper();
		MDC.put("mdcId", UUID.randomUUID());
		log.info("Merchant creation start...");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(merchant));
			logHttpRequest(request, log);
			
			return ResponseEntity.status(HttpStatus.OK).body(merchantService.registerMerchant(log, merchant));
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
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new com.pojo.ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log), merchant));
		} finally {
			log.info("Merchant creation end...");
			MDC.clear();
		}
	}
	
	@RateLimit
	@GetMapping(value = "v1/merchant/{merchant_id}", produces = "application/json")
	@PreAuthorize("hasAnyRole('ROLE_User', 'ROLE_Admin') and hasAnyAuthority('Merchant onboarding & maintenance_read')")
	public ResponseEntity<ApiResponse> getMerchantDetailByMerchant_Id(HttpServletRequest request, @RequestHeader @NotBlank String ip, 
			@PathVariable @NotBlank String merchant_id){
		MDC.put("mdcId", UUID.randomUUID());
		log.info("Get merchant detail start...");
		try {
			logHttpRequest(request, log);

			return ResponseEntity.status(HttpStatus.OK).body(merchantService.getMerchantByMerchant_Id(log, merchant_id));
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
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log)));
		} finally {
			log.info("Get merchant detail end...");
			MDC.clear();
		}
	}
	
	@RateLimit
	@PutMapping(value = "v1/merchant/maintenance/{merchant_id}", produces = "application/json")
	@PreAuthorize("hasAnyRole('ROLE_User', 'ROLE_Admin') and hasAnyAuthority('Merchant onboarding & maintenance_write')")
	public ResponseEntity<ApiResponse> updMerchantByMerchant_Id(HttpServletRequest request, @RequestHeader @NotBlank String ip, 
			@PathVariable @NotBlank String merchant_id, @RequestBody @Valid Merchant merchant){
		MDC.put("mdcId", UUID.randomUUID());
		log.info("Update merchant detail start...");
		try {
			logHttpRequest(request, log);

			return ResponseEntity.status(HttpStatus.OK).body(merchantService.updMerchantByMerchant_Id(log, merchant, merchant_id));
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
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log)));
		} finally {
			log.info("Update merchant detail end...");
			MDC.clear();
		}
	}
	
	@RateLimit
	@DeleteMapping(value = "v1/merchant/maintenance/{merchant_id}", produces = "application/json")
	@PreAuthorize("hasRole('ROLE_Admin') and hasAnyAuthority('Merchant onboarding & maintenance_write')")
	public ResponseEntity<ApiResponse> dltMerchantByMerchant_Id(HttpServletRequest request, @RequestHeader @NotBlank String ip, 
			@PathVariable @NotBlank String merchant_id){
		MDC.put("mdcId", UUID.randomUUID());
		log.info("Delete merchant detail start...");
		try {
			logHttpRequest(request, log);

			return ResponseEntity.status(HttpStatus.OK).body(merchantService.dltMerchantByMerchant_Id(log, merchant_id));
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
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log)));
		} finally {
			log.info("Delete merchant detail end...");
			MDC.clear();
		}
	}
}
