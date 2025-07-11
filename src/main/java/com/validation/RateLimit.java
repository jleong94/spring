package com.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String headerName() default ""; // For limiting based on a specific header
    String pathVariable() default ""; // For limiting based on a path variable
    String requestBodyField() default ""; // For limiting based on a value in the request body
}
