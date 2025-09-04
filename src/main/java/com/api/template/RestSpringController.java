package com.api.template;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import com.pojo.ApiResponse;
import com.pojo.template.Pojo;
import com.service.template.SampleService;
import com.utilities.Tool;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RestSpringController {
	
	private static final Faker faker = new Faker();
	
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());
	
	@Autowired
	Tool tool;
	
	@Autowired
	SampleService sampleService;

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
	@PostMapping(value = "v1/post-template", consumes = {"application/json; charset=UTF-8"}, produces = "application/json; charset=UTF-8")
	@JsonView({Pojo.Post.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	@Validated({Pojo.Post.class}) 
	public ResponseEntity<ApiResponse> postTemplate(HttpServletRequest request, @RequestBody Pojo pojo) throws Exception{
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-Post template start-");
		try {
			logHttpRequest(request, log);
			log.info("Request: " + objectMapper.writeValueAsString(pojo));

			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.pojo(Pojo.builder()
							.id(faker.number().randomDigit())
							.name(faker.name().fullName())
							.ic(sampleService.generateRandomIc())
							.dateOfBirth(faker.date().birthday().toString())
							.password(sampleService.generatePassword(5))
							.account_balance(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 999)))
							.build())
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
			log.info("-Post template end-");
			MDC.clear();
		}
	}
	
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@PostMapping(value = "v1/get-template/{ic}", consumes = {"text/plain"}, produces = "application/json; charset=UTF-8")
	@JsonView({Pojo.Get.class})//Which getter parameter should return within json
	//@Validated - Triggers validation on the annotated object, optionally using specified validation groups.
	@Validated({Pojo.Get.class}) 
	public ResponseEntity<ApiResponse> getTemplate(HttpServletRequest request, @PathVariable @NotBlank String ic) throws Exception{
		MDC.put("mdcId", request.getHeader("mdcId") != null && !request.getHeader("mdcId").isBlank() ? request.getHeader("mdcId") : UUID.randomUUID());
		log.info("-Get template start-");
		try {
			logHttpRequest(request, log);
			log.info("Request: " + ic);

			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.pojo(Pojo.builder()
							.id(faker.number().randomDigit())
							.name(faker.name().fullName())
							.ic(sampleService.generateRandomIc())
							.dateOfBirth(faker.date().birthday().toString())
							.password(sampleService.generatePassword(5))
							.account_balance(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 999)))
							.build())
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
			log.info("-Get template end-");
			MDC.clear();
		}
	}
}
