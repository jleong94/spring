package com.pojo.google;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.NotBlank;
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
	
	public interface Post {}
	
	@Size(groups = {Post.class}, min = 1, message = "Token list is blank.")
	@JsonProperty(value= "tokens", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private List<@NotBlank(groups = {Post.class}, message = "Token is blank.") String> token;

	@NotBlank(groups = {Post.class}, message = "Title is blank.")
	@JsonProperty(value= "title", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String title;
	
	@NotBlank(groups = {Post.class}, message = "Body is blank.")
	@JsonProperty(value= "body", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String body;
	
	@JsonProperty(value= "imageUrl", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String imageUrl;
	
	@JsonProperty(value= "clickAction", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String clickAction; 

	@JsonProperty(value= "data", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private Map<@NotBlank(groups = {Post.class}, message = "data.key is blank.") String, @NotBlank(groups = {Post.class}, message = "data.value is blank.") String> data; 
	
	@JsonProperty(value= "success_count", access = Access.READ_ONLY)
	@JsonView({Post.class})
	private int success_count; 
  
	@JsonProperty(value= "fail_count", access = Access.READ_ONLY)
	@JsonView({Post.class})
	private int fail_count;

	@JsonProperty(value= "resp_data", access = Access.READ_ONLY)
	@JsonView({Post.class})
	private List<Map<String, Object>> resp_data;
}
