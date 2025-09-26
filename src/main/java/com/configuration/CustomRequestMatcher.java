package com.configuration;

import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;

public class CustomRequestMatcher implements RequestMatcher {
	
    private final int port;

    public CustomRequestMatcher(int port) {
        this.port = port;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return request.getLocalPort() == port;
    }
}
