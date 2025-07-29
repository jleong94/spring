package com.validation.keycloak;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequiredActionConstraintValidator implements ConstraintValidator<CredentialTypeValidator, List<String>> {

	private final Set<String> ALLOWED_VALUES = new HashSet<>(Arrays.asList(
			"VERIFY_EMAIL",
			"UPDATE_PASSWORD",
			"UPDATE_PROFILE",
			"CONFIGURE_TOTP",
			"TERMS_AND_CONDITIONS",
			"WEBAUTHN_REGISTER",
			"WEBAUTHN_REGISTER_PASSWORDLESS"
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
