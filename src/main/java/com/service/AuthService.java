package com.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.configuration.CustomAbstractAuthenticationToken;
import com.utilities.JsonMasking;
import com.utilities.LogUtil;
import com.utilities.Tool;

import jakarta.servlet.ServletException;

@Service
public class AuthService {

	private final Tool tool;

	private final JsonMasking jsonMasking;

	/**
	 * Maximum tolerated difference (in milliseconds) between the client supplied
	 * timestamp and server time. Requests outside this window are rejected to limit
	 * the replay window. Defaults to 5 minutes.
	 */
	@Value("${security.signature.max-clock-skew-ms:300000}")
	private long maxClockSkewMs;

	public AuthService(Tool tool, JsonMasking jsonMasking) {
		this.tool = tool;
		this.jsonMasking = jsonMasking;
	}

	/**
	 * Verifies the request signature.
	 *
	 * <p>
	 * The signature must be computed over a canonical string binding the request to
	 * its HTTP method, URI and a timestamp, in addition to the body:
	 * </p>
	 *
	 * <pre>
	 * {method}\n{uri}\n{timestamp}\n{body}
	 * </pre>
	 *
	 * Binding the method, URI and timestamp prevents a captured signature from being
	 * replayed against a different endpoint, and the timestamp freshness check
	 * limits how long a captured request remains usable.
	 *
	 * @param log          logger
	 * @param method       HTTP method of the request
	 * @param uri          request URI
	 * @param timestamp    client supplied epoch-millisecond timestamp (X-TIMESTAMP)
	 * @param requestBody  raw request body
	 * @param signature    Base64 signature to verify
	 * @param signingKeyId identifier of the RSA key to verify against
	 * @return an authenticated token when the signature is valid
	 * @throws ServletException if validation fails for any reason
	 */
	public Authentication isSignatureValid(Logger log, String method, String uri, String timestamp, String requestBody,
			String signature, String signingKeyId) throws ServletException {
		boolean verifySHA256RSA = false;
		try {
			// Log the body with sensitive fields masked (PII/PAN must never be logged raw)
			log.info("Signature body: {}", maskBodyForLog(log, requestBody));
			log.info("Using signing key ID: {}", signingKeyId);
			// Reject malformed URIs instead of bypassing validation
			if (uri == null || uri.isBlank() || uri.contains(" ")) {
				log.warn("Invalid URI detected: {}", uri);
				throw new ServletException("Invalid URI format");
			}
			// Validate the timestamp freshness to limit the replay window
			validateTimestamp(timestamp);
			// Bind the signature to method + URI + timestamp + body to prevent replay
			// against a different endpoint
			String canonical = (method == null ? "" : method) + "\n" + uri + "\n" + timestamp + "\n"
					+ (requestBody == null ? "" : requestBody);
			verifySHA256RSA = tool.verifySHA256RSA(log, canonical, signature, signingKeyId);
			return new CustomAbstractAuthenticationToken(signature, null, verifySHA256RSA, null);
		} catch (Throwable e) {
			LogUtil.logError(log, e);
			throw new ServletException("Signature validation failed: ".concat(e.getMessage()));
		}
	}

	/**
	 * Ensures the supplied timestamp is present, numeric and within the allowed
	 * clock-skew window relative to server time.
	 */
	private void validateTimestamp(String timestamp) throws ServletException {
		if (timestamp == null || timestamp.isBlank()) {
			throw new ServletException("X-TIMESTAMP header is required");
		}
		long requestTime;
		try {
			requestTime = Long.parseLong(timestamp.trim());
		} catch (NumberFormatException e) {
			throw new ServletException("Invalid X-TIMESTAMP format");
		}
		long skew = Math.abs(System.currentTimeMillis() - requestTime);
		if (skew > maxClockSkewMs) {
			throw new ServletException("Request timestamp outside the allowed window");
		}
	}

	/**
	 * Masks sensitive fields in a JSON body for safe logging. Non-JSON or
	 * unparseable bodies are not logged verbatim.
	 */
	private String maskBodyForLog(Logger log, String requestBody) {
		if (requestBody == null || requestBody.isBlank()) {
			return "";
		}
		String trimmed = requestBody.trim();
		// Only attempt JSON masking on payloads that look like JSON; anything else is
		// omitted rather than risk logging sensitive data in the clear.
		if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
			return "[non-JSON body omitted]";
		}
		try {
			return jsonMasking.maskJson(log, trimmed);
		} catch (Throwable e) {
			return "[unmaskable body omitted]";
		}
	}
}
