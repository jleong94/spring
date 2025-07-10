package com.configuration;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * A custom HttpServletRequestWrapper that allows modification of request headers.
 * Useful for cases where headers need to be added or overridden programmatically
 * before reaching downstream filters or controllers.
 */
public class MutableHttpServletRequest extends HttpServletRequestWrapper {
	
	/**
     * A map to store custom headers that override the original request headers.
     */
	private final Map<String, String> customHeaders = new HashMap<>();

	/**
     * Constructs a wrapper for the given HttpServletRequest.
     *
     * @param request the original HttpServletRequest to wrap.
     */
	public MutableHttpServletRequest(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}

	/**
     * Adds or overrides a custom header in the request.
     *
     * @param headerName  the name of the header to add or override.
     * @param headerValue the value of the header.
     */
	public void putHeader(String headerName, String headerValue) {
        customHeaders.put(headerName, headerValue);
    }
	
	/**
     * Returns the value of the specified header. If a custom header exists,
     * it takes precedence over the original.
     *
     * @param name Header name
     * @return Header value
     */
    @Override
    public String getHeader(String name) {
        if (customHeaders.containsKey(name)) {
            return customHeaders.get(name);
        }
        return super.getHeader(name);
    }
    
    /**
     * Returns all the values associated with the given header name.
     * If a custom header is defined, it will return only that value.
     *
     * @param name Header name
     * @return Enumeration of header values
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        if (customHeaders.containsKey(name)) {
            return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
        }
        return super.getHeaders(name);
    }
    
    /**
     * Returns a list of all header names, combining custom and original ones.
     *
     * @return Enumeration of header names
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>();
        // Add original header names
        Enumeration<String> originalHeaderNames = super.getHeaderNames();
        while (originalHeaderNames.hasMoreElements()) {
            names.add(originalHeaderNames.nextElement());
        }
        // Add custom header names
        names.addAll(customHeaders.keySet());
        return Collections.enumeration(names);
    }
}
