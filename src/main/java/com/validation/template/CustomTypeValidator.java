package com.validation.template;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Second param can put primitive or object
public class CustomTypeValidator implements ConstraintValidator<CustomType, Object> {

	private final List<String> customTypes = Arrays.asList("test 1", "test 2");
	
	@Override
    public void initialize(CustomType constraintAnnotation) {
        // Initialization logic if needed
    }
	
	@Override
	// First param can put primitive or object
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		return value == null || value.toString().isBlank() || customTypes.contains(value);
	}

}
