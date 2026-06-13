package com.configuration;
import com.utilities.LogUtil;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.jboss.logging.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.modal.AuditLog;
import com.repo.AuditLogRepo;
import com.validation.Audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Aspect // Tells Spring that this class contains AOP advice
@Component
@Slf4j
public class AuditAspect {

	private final HttpServletRequest request;

	private final AuditLogRepo auditLogRepo;

	public AuditAspect(HttpServletRequest request, AuditLogRepo auditLogRepo) {
		this.request = request;
		this.auditLogRepo = auditLogRepo;
	}

	@AfterReturning("@annotation(audit)")
	@Transactional
	public void afterMethod(JoinPoint joinPoint, Audit audit) throws Throwable {
		try {
			String action = audit.value().isBlank() ? joinPoint.getSignature().getName() : audit.value();
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String username = (auth != null) ? auth.getName() : "SYSTEM";
			String ip = request.getRemoteAddr();
			String userAgent = request.getHeader("User-Agent");
			String xRequestId = request.getHeader("X-Request-ID");
			MDC.put("X-Request-ID", xRequestId);
			String details = Arrays.toString(joinPoint.getArgs());

			// Persist to DB
			@Valid
			AuditLog auditLog = AuditLog.builder()
					.action(action)
					.username(username)
					.ip(ip)
					.user_agent(userAgent)
					.xRequestId(xRequestId)
					.details(details)
					.build();
			auditLogRepo.save(auditLog);
		} catch (Throwable e) {
			LogUtil.logError(log, e);
			throw e;
		} finally {
			MDC.clear();
		}
	}
}
