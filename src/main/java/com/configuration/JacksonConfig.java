package com.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
				// Ignore unknown properties during deserialization
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				// ACCEPT_EMPTY_STRING_AS_NULL_OBJECT:
				// Treats empty strings ("") as null when deserializing into Object types.
				// Example: {"user": ""} will deserialize user field as null instead of empty string
				// Useful when APIs return empty strings instead of null for missing/empty objects
				// Without this: empty string would cause deserialization errors for complex types
				.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
				// Case-insensitive deserialization
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
				// Case-insensitive deserialization
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				// FAIL_ON_EMPTY_BEANS:
				// By default, Jackson throws an exception when serializing objects with no properties
				// Disabling this allows serializing "empty" beans (classes with no getters/fields) as {}
				// Example: public class EmptyClass {} will serialize as {} instead of throwing exception
				// Useful when dealing with marker classes, DTOs that may be empty, or dynamic objects
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				// Exclude null & blank values from serialization
				.serializationInclusion(JsonInclude.Include.NON_EMPTY)
				// Handle Java 8 date/time types
				.addModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.build();
	}
}
