package com.configuration;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PermissionEvaluatorHandler implements PermissionEvaluator {

	/**
	 * Custom authorization logic.
	 * Spring Security calls this method before executing a protected method.
	 * 
	 * Example usage from controller:
	 *   @PreAuthorize("@permissionEvaluator.hasPermission(authentication, 'Email', 'read')")
	 * 
	 * Example values at runtime:
	 *   authentication.getPrincipal() → JWT of user
	 *   targetDomainObject            → "Email"
	 *   permission                    → "read"
	 */
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		MDC.put("mdcId", UUID.randomUUID());
		boolean result = false;
		try {
			log.info("-Permission evaluator handler start-");
			if (!(authentication.getPrincipal() instanceof Jwt jwt)) return false;

			String username = jwt.getClaimAsString("preferred_username");
			List<String> roles = jwt.getClaimAsStringList("realm_access.roles");

			if(username != null && !username.isBlank() && !roles.isEmpty()) {

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
			throw e;
		} finally {
			log.info("-Permission evaluator handler end-");
			MDC.clear();
		}
		return result;
	}

	/**
	 * Method used when @PreAuthorize uses: hasPermission(targetId, targetType, permission)
	 * 
	 * Example usage:
	 *   @PreAuthorize("hasPermission(#documentId, 'Document', 'edit')")
	 * 
	 * Example runtime values:
	 *   targetId       = 123 (Document ID)
	 *   targetType     = "Document"
	 *   permission     = "edit"
	 *   authentication = JWT of user
	 */
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		MDC.put("mdcId", UUID.randomUUID());
		boolean result = false;
		try {
			log.info("-Permission evaluator handler start-");
			
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
			log.info("-Permission evaluator handler end-");
			MDC.clear();
		}
		return result;
	}

}
