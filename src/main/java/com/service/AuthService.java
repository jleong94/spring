package com.service;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.configuration.CustomAbstractAuthenticationToken;
import com.utilities.Tool;

import jakarta.servlet.ServletException;

@Service
public class AuthService {

	private final Tool tool;

	public AuthService(Tool tool) {
		this.tool = tool;
	}

	public Authentication isSignatureValid(Logger log, String uri, String requestBody, String signature,
			String signingKeyId) throws ServletException {
		boolean verifySHA256RSA = false;
		try {
			log.info("Plain signature body: {}", requestBody);
			log.info("Using signing key ID: {}", signingKeyId);
			// Reject malformed URIs instead of bypassing validation
			if (uri == null || uri.isBlank() || uri.contains(" ")) {
				log.warn("Invalid URI detected: {}", uri);
				throw new ServletException("Invalid URI format");
			}
			verifySHA256RSA = tool.verifySHA256RSA(log, requestBody, signature, signingKeyId);
			return new CustomAbstractAuthenticationToken(signature, null, verifySHA256RSA, null);
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
			throw new ServletException("Signature validation failed: ".concat(e.getMessage()));
		}
	}
}
