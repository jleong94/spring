package com.modal;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@Entity(name = "auditlog")
@Table(name = "auditlog",//Table name 
catalog = "appdb",//DB name 
schema = "",//Only for MSSQL, dbo
indexes = {
		@Index(name = "idx_auditlog_action", columnList = "action"),
		@Index(name = "idx_auditlog_username", columnList = "username"),
		@Index(name = "idx_auditlog_details", columnList = "details"),
		@Index(name = "idx_auditlog_timestamp", columnList = "timestamp DESC")
})
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "audit_id", unique = true, nullable = false, insertable = true, updatable = false, table = "auditlog")
	private Long id;

	@NotBlank
	@Column(name = "action", unique = false, nullable = false, insertable = true, updatable = false, table = "auditlog", length = 255)
	private String action;

	@Column(name = "username", unique = false, nullable = false, insertable = true, updatable = false, table = "auditlog", length = 50)
	private String username;

	@Size(max = 15, message = "IP exceeded max length(15) allowed.") // max length for IPv4 string
	@Pattern(
			regexp = "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.|$)){4}$",
			message = "Invalid IP address"
			)
	@Column(name = "ip", unique = false, nullable = false, insertable = true, updatable = false, table = "auditlog", length = 15)
	private String ip;

	@Column(name = "user_agent", unique = false, nullable = false, insertable = true, updatable = false, table = "auditlog", length = 1024)
	private String user_agent;

	@Column(name = "x_request_id", unique = false, nullable = false, insertable = true, updatable = false, table = "auditlog", length = 255)
	private String xRequestId;

	@Column(name = "details", unique = false, nullable = false, insertable = true, updatable = false, table = "auditlog", length = 768)
	private String details;

	@CreationTimestamp
	@Column(name = "timestamp", unique = false, nullable = false, insertable = true, updatable = false, table = "auditlog")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime timestamp;
}
