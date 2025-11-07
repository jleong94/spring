package com.pojo.google;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
public class Message {
	
	private List<String> token;

	private String title;
	
    private String body;
	   
    private String imageUrl;
	
    private String clickAction; 

    private Map<String, String> data; 
	  
    private int success_count; 
  
    private int fail_count;

    private List<Map<String, Object>> resp_data;
}
