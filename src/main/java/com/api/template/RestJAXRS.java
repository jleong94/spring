/*
package com.api.template;

import java.util.Enumeration;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.MDC;
import org.json.JSONObject;
import org.slf4j.Logger;

@Slf4j
@Path("/api/test")
public class RestJAXRS {

	private void logHttpRequest(HttpServletRequest request, Logger log) {
		if (request == null || log == null) {return;}
		try {
			// Log request headers
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
		} catch (Throwable e) {
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

	@POST @GET @PUT @DELETE
	@Path("/token/{x3}")
	@Consumes({"application/json; charset=UTF-8"})
	@Produces({"application/json; charset=UTF-8"})
	protected Response methodName(@Context HttpServletRequest request,
			@HeaderParam("authorization") @DefaultValue("") String authorization,//Values from header
			@CookieParam("session") @DefaultValue("") String session,//Values from cookies
			@QueryParam("x2") @DefaultValue("") String x2,// /token?x2=test & with default value empty string
			@PathParam("x3") @DefaultValue("") String x3,// /token/{x3}			
			@FormParam("x4") @DefaultValue("") String x4,//Values from form submission
			String x) {
		MDC.put("X-Request-ID", request.getHeader("X-Request-ID") != null && !request.getHeader("X-Request-ID").isBlank() ? request.getHeader("X-Request-ID") : UUID.randomUUID());
		log.info("-start-".concat(x));
		Status httpStatus = Status.OK;
		JSONObject requestJson = null, responseJson = null;
		try {
			logHttpRequest(request, log);
			requestJson = new JSONObject(x);
			
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
		} finally{
			try {
				log.info("-end-".concat(responseJson.toString()));
			} catch(Throwable e) {}
		}
		return Response.status(httpStatus).entity(responseJson.toString()).build();
	}
}
*/
