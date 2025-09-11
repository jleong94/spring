package com.modal;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Entity(name = "email")
@Table(name = "email",//Table name 
catalog = "appdb",//DB name 
schema = ""//Only for MSSQL, dbo
		)
@Audited//log changes into another table automatically
public class Email {
	
	public interface SendEmail {}
	public interface GetEmailDetailById {}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "mail_id", unique = true, nullable = false, insertable = true, updatable = false, table = "email", columnDefinition = "BIGINT")
	private Long mail_id;
	
	@CreationTimestamp
    @Column(name = "created_datetime", unique = false, nullable = false, insertable = true, updatable = false, table = "email", columnDefinition = "DATETIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime created_datetime;
	
	@UpdateTimestamp
	@Column(name = "modified_datetime", unique = false, nullable = false, insertable = true, updatable = true, table = "email", columnDefinition = "DATETIME")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime modified_datetime;
	
	@Column(name = "sender", unique = false, nullable = false, insertable = true, updatable = false, table = "email", columnDefinition = "VARCHAR(255)")
	private String sender;
	
	@Size(groups = {SendEmail.class}, max = 255, message = "Receiver email exceed 255 characters.")
	@Column(name = "receiver", unique = false, nullable = false, insertable = true, updatable = true, table = "email", columnDefinition = "VARCHAR(255)")
	private String receiver;
	
	@Size(groups = {SendEmail.class}, max = 255, message = "CC email exceed 255 characters.")
	@Column(name = "cc", unique = false, nullable = true, insertable = true, updatable = true, table = "email", columnDefinition = "VARCHAR(255)")
	private String cc;

	@Size(groups = {SendEmail.class}, max = 255, message = "BCC email exceed 255 characters.")
	@Column(name = "bcc", unique = false, nullable = true, insertable = true, updatable = true, table = "email", columnDefinition = "VARCHAR(255)")
	private String bcc;

	@NotBlank(groups = {SendEmail.class}, message = "Email subject is blank.")
	@Size(groups = {SendEmail.class}, max = 255, message = "Email subject exceed 255 characters.")
	@Column(name = "subject", unique = false, nullable = false, insertable = true, updatable = true, table = "email", columnDefinition = "VARCHAR(255)")
	private String subject;

	@NotBlank(groups = {SendEmail.class}, message = "Email body is blank.")
	@Size(groups = {SendEmail.class}, max = 1000, message = "Email body exceed 1000 characters.")
	@Column(name = "body", unique = false, nullable = false, insertable = true, updatable = true, table = "email", columnDefinition = "VARCHAR(1000)")
	private String body;
	
	@OneToMany(targetEntity = EmailAttachment.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "email")
	private List<EmailAttachment> attachments;

	@Column(name = "isSend", unique = false, nullable = false, insertable = true, updatable = true, table = "email", columnDefinition = "BOOLEAN")
	private boolean isSend;
}
