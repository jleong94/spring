package com.validation.keycloak;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CredentialTypeConstraintValidator implements ConstraintValidator<CredentialTypeValidator, List<String>> {

	private final Set<String> ALLOWED_VALUES = new HashSet<>(Arrays.asList(
			"password",
			"otp",
			"webauthn",
			"webauthn-passwordless",
			"authenticator"
			));

	@Override
	public boolean isValid(List<String> value, ConstraintValidatorContext context) {
		if (value == null) {
			return true; // Consider null as valid; use @NotNull for null check
		}
		for (String item : value) {
			if (!ALLOWED_VALUES.contains(item)) {
				return false;
			}
		}
		return true;
	}

}
