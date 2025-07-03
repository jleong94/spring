package com.configuration;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {
	
	private final Map<Object, Object> customHeaders = new HashMap<>();

	public MutableHttpServletRequest(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}

	public void putHeader(Object headerName, Object headerValue) {
        customHeaders.put(headerName, headerValue);
    }
}
