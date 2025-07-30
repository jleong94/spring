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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modal.EMail;
import com.pojo.ApiResponse;
import com.pojo.Property;
import com.service.EMailService;
import com.utilities.Tool;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class Rest_EMail {
	
	@Autowired
	Property property;
	
	@Autowired
	Tool tool;
	
	@Autowired
	EMailService emailService;
	
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

	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "receiver,cc,bcc")
	@PostMapping(value = "v1/email/send", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({EMail.SendEMail.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	public ResponseEntity<ApiResponse> sendEMail(HttpServletRequest request, @RequestBody @Validated({EMail.SendEMail.class}) EMail email, @RequestParam(required = false) MultipartFile[] upload_files) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-Send email start-");
		try {
			log.info("Request: " + objectMapper.writeValueAsString(email));
			logHttpRequest(request, log);
			
			email = emailService.saveUploadFileToPath(upload_files, email);
			email = emailService.sendEMail(email);
			
			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.email(email)
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
			log.info("-Send email end-");
			MDC.clear();
		}
	}
	
	@RateLimit(headerName = "", pathVariable = "mail_id", requestBodyField = "")
	@GetMapping(value = "v1/email/check/{mail_id}", produces = "application/json; charset=UTF-8")
	@JsonView({EMail.GetEmailDetailById.class})//Which getter parameter should return within json
	public ResponseEntity<ApiResponse> getMerchantDetailByMerchant_Id(HttpServletRequest request, @PathVariable @NotBlank Long mail_id) throws Exception{
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-Get sent email detail start-");
		try {
			logHttpRequest(request, log);

			return ResponseEntity.status(HttpStatus.OK).body(emailService.getMerchantDetailByMerchant_Id(mail_id));
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
			log.info("-Get sent email detail end-");
			MDC.clear();
		}
	}
}
