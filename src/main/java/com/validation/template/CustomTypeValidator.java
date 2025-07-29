package com.validation.template;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomTypeValidator implements ConstraintValidator<CustomType, String> {

	private final List<String> customTypes = Arrays.asList("test 1", "test 2");
	
	@Override
    public void initialize(CustomType constraintAnnotation) {
        // Initialization logic if needed
    }
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		return value == null || value.isBlank() || customTypes.contains(value);
	}

}
