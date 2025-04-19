package com.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = CustomTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomType {
	String message() default "Type must be either 'test 1' or 'test 2'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
