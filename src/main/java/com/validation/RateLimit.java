package com.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
	int capacity() default 0;
	int tokens() default 0;
	int period() default 0;
    String ip() default "ip"; // Default is to rate limit by IP
    String headerName() default ""; // For limiting based on a specific header
    String pathVariable() default ""; // For limiting based on a path variable
    String requestBodyField() default ""; // For limiting based on a value in the request body
}
