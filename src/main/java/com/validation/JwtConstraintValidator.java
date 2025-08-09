package com.validation;

import com.pojo.Jwt;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class JwtConstraintValidator implements ConstraintValidator<JwtValidator, Jwt> {

	@Override
	public boolean isValid(Jwt jwt, ConstraintValidatorContext context) {
		if ("password".equalsIgnoreCase(jwt.getGrant_type())) {
			if (jwt.getUsername() == null || jwt.getUsername().isBlank()) {
				context.buildConstraintViolationWithTemplate("Username is blank.")
				.addPropertyNode("username")
				.addConstraintViolation();
				return false;
			} if (jwt.getPassword() == null || jwt.getPassword().isBlank()) {
				context.buildConstraintViolationWithTemplate("Password is blank.")
				.addPropertyNode("password")
				.addConstraintViolation();
				return false;
			}
			return true;
		} else if ("refresh_token".equalsIgnoreCase(jwt.getGrant_type())) {
			if (jwt.getRefresh_token() == null || jwt.getRefresh_token().isBlank()) {
				context.buildConstraintViolationWithTemplate("Refresh token is blank.")
				.addPropertyNode("refresh_token")
				.addConstraintViolation();
				return false;
			}
			return true;
		} else {
			context.buildConstraintViolationWithTemplate("Invalid grant type.")
			.addPropertyNode("grant_type")
			.addConstraintViolation();
			return false;
		}
	}

}