package com.pojo.google;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "FCM request response payload")
public class Message {
	
	public interface Post {}
	
	@Size(groups = {Post.class}, min = 1, message = "Token list is blank.")
	@JsonProperty(value= "tokens", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "FCM token.")
	private List<@NotBlank(groups = {Post.class}, message = "Token is blank.") String> token;

	@NotBlank(groups = {Post.class}, message = "Title is blank.")
	@JsonProperty(value= "title", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "Title.")
	private String title;
	
	@NotBlank(groups = {Post.class}, message = "Body is blank.")
	@JsonProperty(value= "body", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "Body.")
    private String body;
	
	@JsonProperty(value= "imageUrl", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "Image URL.")
    private String imageUrl;
	
	@JsonProperty(value= "clickAction", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "Click action.")
    private String clickAction; 

	@JsonProperty(value= "data", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "Payload.")
    private Map<@NotBlank(groups = {Post.class}, message = "data.key is blank.") String, @NotBlank(groups = {Post.class}, message = "data.value is blank.") String> data; 
	
	@JsonProperty(value= "success_count", access = Access.READ_ONLY)
	@JsonView({Post.class})
	@Schema(description = "Success count.")
    private int success_count; 
  
	@JsonProperty(value= "fail_count", access = Access.READ_ONLY)
	@JsonView({Post.class})
	@Schema(description = "Fail count.")
    private int fail_count;

	@JsonProperty(value= "resp_data", access = Access.READ_ONLY)
	@JsonView({Post.class})
	@Schema(description = "Response data.")
    private List<Map<String, Object>> resp_data;
}
