package com.validation.keycloak;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CredentialTypeConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CredentialTypeValidator {

	String message() default "Invalid credential type found";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
