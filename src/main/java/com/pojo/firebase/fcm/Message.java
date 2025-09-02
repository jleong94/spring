package com.pojo.firebase.fcm;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Message {
	
	@NotEmpty(message = "Tokens list is empty") // ensures not null & size > 0
    @Size(min = 1, message = "Tokens list must contain at least one token")
	@JsonProperty(value= "tokens", access = Access.WRITE_ONLY)
	private List<String> token;

	@JsonProperty(value= "title", access = Access.WRITE_ONLY)
	private String title;
	
	@JsonProperty(value= "body", access = Access.WRITE_ONLY)
    private String body;
	
	@JsonProperty(value= "imageUrl", access = Access.WRITE_ONLY)      
    private String imageUrl;
	
	@JsonProperty(value= "clickAction", access = Access.WRITE_ONLY)   
    private String clickAction; 

	@JsonProperty(value= "data", access = Access.WRITE_ONLY)  
    private Map<String, String> data; 
	
	@JsonProperty(value= "success_count", access = Access.READ_ONLY)  
    private int success_count; 
	
	@JsonProperty(value= "fail_count", access = Access.READ_ONLY)  
    private int fail_count;

	@JsonProperty(value= "resp_data", access = Access.READ_ONLY)  
    private List<Map<String, Object>> resp_data;
}
