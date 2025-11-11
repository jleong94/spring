package com.utilities;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility component for masking sensitive fields in JSON data.
 * This class provides functionality to mask specified fields in JSON structures
 * while preserving the original JSON structure and handling nested objects and arrays.
 * Thread-safe implementation using ConcurrentHashMap for field storage.
 *
 * @author jleong94
 * @since 2025-11-06
 */
@Component
public class JsonMasking {

	/** ObjectMapper instance for JSON processing */
	private final ObjectMapper objectMapper;

	/** Thread-safe set of field names to be masked */
	private Set<String> fields2Mask = ConcurrentHashMap.newKeySet();

	/**
	 * Constructor initializing the ObjectMapper with JavaTimeModule support.
	 * 
	 * @param objectMapper Jackson ObjectMapper instance
	 */
	public JsonMasking(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Adds multiple field names to the set of fields to be masked.
	 * 
	 * @param fields2Mask Set of field names to be added to masking set
	 */
	public void addFields2Mask(Set<String> fields2Mask) {
		this.fields2Mask.addAll(fields2Mask);
	}

	/**
	 * Masks sensitive fields in the provided JSON string.
	 * Includes detailed error logging with stack trace information.
	 *
	 * @param log SLF4J Logger instance for error logging
	 * @param jsonString Input JSON string to be masked
	 * @return Masked JSON string
	 * @throws Throwable if any error occurs during JSON processing
	 */
	public String maskJson(Logger log, String jsonString) throws Throwable {
		try {
			JsonNode rootNode = objectMapper.readTree(jsonString);
			JsonNode maskedNode = maskNode(rootNode);
			return objectMapper.writeValueAsString(maskedNode);
		} catch(Throwable e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			throw e;
		}
	}

	/**
	 * Recursively masks a JsonNode based on its type (object or array).
	 * 
	 * @param node JsonNode to be masked
	 * @return Masked JsonNode
	 */
	private JsonNode maskNode(JsonNode node) {
		if (node.isObject()) {
			return maskObjectNode((ObjectNode) node);
		} else if (node.isArray()) {
			return maskArrayNode((ArrayNode) node);
		}
		return node;
	}

	/**
	 * Masks sensitive fields within an ObjectNode.
	 * Handles nested objects and arrays recursively while masking designated fields.
	 * 
	 * @param objectNode ObjectNode to be processed
	 * @return Masked ObjectNode
	 */
	private ObjectNode maskObjectNode(ObjectNode objectNode) {
		ObjectNode maskedObject = objectMapper.createObjectNode();
		Iterator<String> fieldNames = objectNode.fieldNames();

		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode fieldValue = objectNode.get(fieldName);

			if (shouldMaskField(fieldName)) {
				// Mask the field value by replacing with asterisks of the same length
				maskedObject.put(fieldName, "*".repeat(maskedObject.get(fieldName).toString().length()));
			} else if (fieldValue.isObject()) {
				maskedObject.set(fieldName, maskObjectNode((ObjectNode) fieldValue));
			} else if (fieldValue.isArray()) {
				maskedObject.set(fieldName, maskArrayNode((ArrayNode) fieldValue));
			} else {
				maskedObject.set(fieldName, fieldValue);
			}
		}

		return maskedObject;
	}

	/**
	 * Masks elements within an ArrayNode.
	 * Recursively processes array elements that are objects or nested arrays.
	 * 
	 * @param arrayNode ArrayNode to be processed
	 * @return Masked ArrayNode
	 */
	private ArrayNode maskArrayNode(ArrayNode arrayNode) {
		ArrayNode maskedArray = objectMapper.createArrayNode();

		for (JsonNode element : arrayNode) {
			if (element.isObject()) {
				maskedArray.add(maskObjectNode((ObjectNode) element));
			} else if (element.isArray()) {
				maskedArray.add(maskArrayNode((ArrayNode) element));
			} else {
				maskedArray.add(element);
			}
		}

		return maskedArray;
	}

	/**
	 * Determines if a field should be masked based on its name.
	 * Field names are compared case-insensitively.
	 * 
	 * @param fieldName Name of the field to check
	 * @return true if the field should be masked, false otherwise
	 */
	private boolean shouldMaskField(String fieldName) {
		return this.fields2Mask.contains(fieldName.toLowerCase());
	}
}