package com.modal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder
@Entity(name = "email")
@Table(name = "email", schema = "appdb")
public class EMail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "mail_id", nullable = false)
	@JsonIgnore
	private Long mail_id;
	
	@NotNull(message = "Sender email is null.")
	@NotBlank(message = "Sender email is blank.")
	@Size(max = 255, message = "Sender email exceed 255 characters.")
	@Column(name = "from", length = 255, nullable = false)
	@JsonIgnore
	private String from;
	
	@Size(max = 255, message = "Receiver email exceed 255 characters.")
	@Column(name = "to", length = 255)
	@JsonProperty(value= "to", access = Access.WRITE_ONLY)
	private String to;
	
	@Size(max = 255, message = "CC email exceed 255 characters.")
	@Column(name = "cc", length = 255)
	@JsonProperty(value= "cc", access = Access.WRITE_ONLY)
	private String cc;

	@Size(max = 255, message = "BCC email exceed 255 characters.")
	@Column(name = "bcc", length = 255)
	@JsonProperty(value= "bcc", access = Access.WRITE_ONLY)
	private String bcc;

	@Size(max = 500, message = "Email subject exceed 500 characters.")
	@Column(name = "subject", length = 500)
	@JsonProperty(value= "subject", access = Access.WRITE_ONLY)
	private String subject;
	
	@Size(max = 1000, message = "Email body exceed 1000 characters.")
	@Column(name = "body", length = 1000)
	@JsonProperty(value= "body", access = Access.WRITE_ONLY)
	private String body;
	
	@OneToMany(targetEntity = EMailAttachment.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "email")
	@JsonProperty(value= "attachments", access = Access.WRITE_ONLY)
	private List<EMailAttachment> attachments;
	
	@Transient
	@JsonProperty(value= "isHTML", access = Access.WRITE_ONLY)
	private boolean isHTML;
	
	@Min(0)
    @Max(1)
	@Column(name = "isSend", nullable = false)
	@JsonIgnore
	private int isSend;
}
