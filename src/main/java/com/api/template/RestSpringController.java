package com.api.template;

// Rest controller import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import com.pojo.ApiResponse;
import com.pojo.template.Pojo;
import com.service.template.SampleService;
import com.utilities.Tool;
import com.validation.Audit;
import com.validation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Slf4j//Only use .info/error for technical/system logs
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
		}
	}

	@Audit("POST-TEMPLATE")
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@PostMapping(value = "v1/post-template", consumes = {MediaType.APPLICATION_JSON}, produces = {MediaType.APPLICATION_JSON})
	@JsonView({Pojo.Post.class})//Which getter parameter should return within json
	@Validated({Pojo.Post.class})//Triggers validation on parameter where annotation validation apply with groups = {}.
	@Transactional
	public ResponseEntity<ApiResponse> postTemplate(HttpServletRequest request, @RequestBody Pojo pojo) throws Throwable{
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
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
			log.info("-Post template end-");
			MDC.clear();
		}
	}

	@Audit("GET-TEMPLATE")
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@GetMapping(value = "v1/get-template/{ic}", produces = {MediaType.APPLICATION_JSON})
	@JsonView({Pojo.Get.class})//Which getter parameter should return within json
	@Validated({Pojo.Get.class})//Triggers validation on parameter where annotation validation apply with groups = {}.
	@Transactional
	public ResponseEntity<ApiResponse> getTemplate(HttpServletRequest request, @PathVariable @NotBlank String ic) throws Throwable{
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-Get template start-");
		try {
			logHttpRequest(request, log);

			return ResponseEntity.status(HttpStatus.FOUND).body(ApiResponse
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
			log.info("-Get template end-");
			MDC.clear();
		}
	}

	@Audit("PUT-TEMPLATE")
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@PutMapping(value = "v1/put-template/{id}/{ic}", consumes = {MediaType.APPLICATION_JSON}, produces = {MediaType.APPLICATION_JSON})
	@JsonView({Pojo.Put.class})//Which getter parameter should return within json
	@Validated({Pojo.Put.class})//Triggers validation on parameter where annotation validation apply with groups = {}.
	@Transactional
	public ResponseEntity<ApiResponse> putTemplate(HttpServletRequest request, @PathVariable @NotBlank int id, @PathVariable @NotBlank String ic, @RequestBody Pojo pojo) throws Throwable{
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-Put template start-");
		try {
			logHttpRequest(request, log);
			log.info("Request: " + objectMapper.writeValueAsString(pojo));

			return ResponseEntity.status(HttpStatus.OK).body(sampleService.putTemplate(log, id, ic, pojo));
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
			log.info("-Put template end-");
			MDC.clear();
		}
	}

	@Audit("DELETE-TEMPLATE")
	@RateLimit(headerName = "", pathVariable = "", requestBodyField = "")
	@DeleteMapping(value = "v1/delete-template", produces = {MediaType.APPLICATION_JSON})
	@JsonView({Pojo.Delete.class})//Which getter parameter should return within json
	@Validated({Pojo.Delete.class})//Triggers validation on parameter where annotation validation apply with groups = {}.
	@Transactional
	public ResponseEntity<ApiResponse> putTemplate(HttpServletRequest request, @RequestParam int ic) throws Throwable{
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-Delete template start-");
		try {
			logHttpRequest(request, log);

			return ResponseEntity.status(HttpStatus.RESET_CONTENT).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
					.datetime(tool.getTodayDateTimeInString())
					.build());
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
			log.info("-Delete template end-");
			MDC.clear();
		}
	}
}
