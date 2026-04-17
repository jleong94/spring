package com.pojo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EmailDTO {

	private Long mailId;
	private LocalDateTime createdDatetime;
	private LocalDateTime modifiedDatetime;
	private String sender;
	private String receiver;
	private String cc;
	private String bcc;
	private String subject;
	private String body;
	private boolean send;
	private List<EmailAttachmentDTO> attachments;
}
