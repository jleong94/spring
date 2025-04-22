package com.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import com.properties.Property;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private Property property;

    private LoadingCache<String, RateLimiter> rateLimiters;

    @Autowired
    public void init() {
        this.rateLimiters = CacheBuilder.newBuilder()
                .expireAfterAccess(property.getRate_limit_reset(), TimeUnit.valueOf(property.getRate_limit_unit()))
                .build(new CacheLoader<String, RateLimiter>() {
                    @Override
                    public RateLimiter load(@Nonnull String key) {
                        return RateLimiter.create((double) property.getRate_limit_requests());
                    }
                });
    }

    /**
     * Aspect method that handles rate limiting for endpoints annotated with @RateLimit
     * 
     * @param joinPoint The join point representing the intercepted method
     * @return The result of proceeding with the method execution
     * @throws Throwable if rate limit is exceeded or other errors occur
     */
    @Around("@annotation(com.validation.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the current HTTP request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // Extract the client IP address
        String ip = getClientIP(request);
        
        // Get or create a rate limiter for this IP address
        RateLimiter rateLimiter = rateLimiters.get(ip);
        // Check if request is allowed under rate limit
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitExceededException("Rate limit exceeded. Maximum " + 
                property.getRate_limit_requests() + " requests per second allowed.");
        }
        // If within rate limit, proceed with the method execution
        return joinPoint.proceed();
    }

    /**
     * Extracts the client IP address from the request
     * First checks the X-Forwarded-For header which contains the original client IP when behind a proxy
     * Falls back to request.getRemoteAddr() if X-Forwarded-For is not present
     * For X-Forwarded-For, takes the first IP in the list which represents the original client
     *
     * @param request The HTTP servlet request
     * @return The client's IP address as a string
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
