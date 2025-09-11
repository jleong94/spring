package com.modal;

import org.hibernate.envers.Audited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@Entity(name = "email_attachment")
@Table(name = "email_attachment",//Table name 
catalog = "appdb",//DB name 
schema = ""//Only for MSSQL, dbo
		)
@Audited//log changes into another table automatically
public class EmailAttachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "attachment_id", unique = true, nullable = false, insertable = true, updatable = false, table = "email_attachment", columnDefinition = "BIGINT")
	private Long attachment_id;
	
	@Size(max = 255, message = "Mail attachement file path exceed 255 characters.")
	@Column(name = "file_path", unique = false, nullable = true, insertable = true, updatable = false, table = "email_attachment", columnDefinition = "VARCHAR(255)")
	private String file_path;
	
	@ManyToOne(targetEntity = Email.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "mail_id", referencedColumnName = "mail_id", unique = false, nullable = false)
	private Email email;
}
