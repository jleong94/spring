package com.service;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.configuration.CustomAbstractAuthenticationToken;
import com.pojo.ApiKey;
import com.utilities.Tool;

import jakarta.servlet.ServletException;

@Service
public class AuthService {
	
	private final Tool tool;

	private final ApiKey apiKey;

	public AuthService(Tool tool, ApiKey apiKey) {
		this.tool = tool;
		this.apiKey = apiKey;
	}

	public Authentication isSignatureValid(Logger log, String uri, String requestBody, String signature) throws ServletException {
		boolean verifySHA256RSA = false;
		try {
			log.info("Plain signature body: {}", requestBody);
			if(uri.contains(" ")) {
				
			} else {verifySHA256RSA = tool.verifySHA256RSA(log, apiKey.getGeneral(), requestBody, signature);}
			return new CustomAbstractAuthenticationToken(signature, null, verifySHA256RSA);
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
			throw new ServletException("Signature validation failed: ".concat(e.getMessage()));
		}
	}
}
