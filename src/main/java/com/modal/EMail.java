package com.modal;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
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
@Table(name = "email", schema = "appdb")
public class EMail {
	
	public interface SendEMail {}
	public interface GetMerchantDetailByMerchant_Id {}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "mail_id", unique = true, nullable = false, insertable = true, updatable = false, table = "email")
	@JsonProperty(value= "mail_id", access = Access.READ_ONLY)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private Long mail_id;
	
	@CreationTimestamp
    @Column(name = "created_datetime", unique = false, nullable = false, insertable = true, updatable = false, table = "email")
	@JsonProperty(value= "created_datetime", access = Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private LocalDateTime created_datetime;
	
	@UpdateTimestamp
	@Column(name = "modified_datetime", unique = false, nullable = false, insertable = true, updatable = true, table = "email")
	@JsonProperty(value= "modified_datetime", access = Access.READ_ONLY)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private LocalDateTime modified_datetime;
	
	@Column(name = "sender", unique = false, nullable = false, insertable = true, updatable = false, table = "email", length = 255)
	@JsonIgnore
	private String sender;
	
	@Size(groups = {SendEMail.class}, max = 255, message = "Receiver email exceed 255 characters.")
	@Column(name = "receiver", unique = false, nullable = false, insertable = true, updatable = true, table = "email", length = 255)
	@JsonProperty(value= "receiver", access = Access.READ_WRITE)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private String receiver;
	
	@Size(groups = {SendEMail.class}, max = 255, message = "CC email exceed 255 characters.")
	@Column(name = "cc", unique = false, nullable = true, insertable = true, updatable = true, table = "email", length = 255)
	@JsonProperty(value= "cc", access = Access.READ_WRITE)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private String cc;

	@Size(groups = {SendEMail.class}, max = 255, message = "BCC email exceed 255 characters.")
	@Column(name = "bcc", unique = false, nullable = true, insertable = true, updatable = true, table = "email", length = 255)
	@JsonProperty(value= "bcc", access = Access.READ_WRITE)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private String bcc;

	@NotBlank(groups = {SendEMail.class}, message = "Email subject is blank.")
	@Size(groups = {SendEMail.class}, max = 500, message = "Email subject exceed 500 characters.")
	@Column(name = "subject", unique = false, nullable = false, insertable = true, updatable = true, table = "email", length = 500)
	@JsonProperty(value= "subject", access = Access.READ_WRITE)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private String subject;

	@NotBlank(groups = {SendEMail.class}, message = "Email body is blank.")
	@Size(groups = {SendEMail.class}, max = 1000, message = "Email body exceed 1000 characters.")
	@Column(name = "body", unique = false, nullable = false, insertable = true, updatable = true, table = "email", length = 1000)
	@JsonProperty(value= "body", access = Access.READ_WRITE)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private String body;
	
	@OneToMany(targetEntity = EMailAttachment.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "email")
	@JsonIgnore
	private List<EMailAttachment> attachments;
	
	@Column(name = "isHTML", unique = false, nullable = false, insertable = true, updatable = true, table = "email")
	@JsonProperty(value= "isHTML", access = Access.READ_WRITE)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private boolean isHTML;

	@Column(name = "isSend", unique = false, nullable = false, insertable = true, updatable = true, table = "email")
	@JsonProperty(value= "isSend", access = Access.READ_ONLY)
	@JsonView({SendEMail.class, GetMerchantDetailByMerchant_Id.class})
	private boolean isSend;
}
