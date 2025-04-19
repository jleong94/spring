package com.modal.onboard;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.modal.UserStatusLookup;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "merchant")
@Table(name = "merchant", schema = "appdb",
indexes = {
		@Index(name = "idx_merchant_id", columnList = "merchant_id")
})
public class Merchant {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "id", nullable = false)
	@JsonIgnore
	private Long id;

	@JsonProperty(value= "merchant_id", access = Access.READ_ONLY)
	@Column(name = "merchant_id", nullable = false, length = 15, unique = true)
	private String merchant_id;

	@JsonProperty(value= "datetime", access = Access.READ_ONLY)
	@Column(name = "created_datetime", nullable = false)
	private String created_datetime;

	@Column(name = "modified_datetime", nullable = false)
	@JsonIgnore
	private String modified_datetime;

	@JsonProperty(value= "merchant_name", access = Access.READ_WRITE)
	@NotNull(message = "Merchant name is null.")
	@NotBlank(message = "Merchant name is blank.")
	@Size(max = 50, message = "Merchant name exceed 50 characters.")
	@Column(name = "merchant_name", length = 50, nullable = false)
	private String merchant_name;

	@JsonProperty(value= "dba_name", access = Access.READ_WRITE)
	@NotNull(message = "DBA name is null.")
	@NotBlank(message = "DBA name is blank.")
	@Size(max = 25, message = "DBA name exceed 25 characters.")
	@Column(name = "dba_name", length = 25, nullable = false)
	private String dba_name;

	@JsonProperty(value= "merchant_status", access = Access.READ_ONLY)
	@ManyToOne(targetEntity = UserStatusLookup.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "status_id", referencedColumnName = "status_id", unique = false, nullable = false)
	/*@JoinTable(name = "merchant_user_status", 
	joinColumns = {@JoinColumn(name = "merchant_id", referencedColumnName = "id", unique = false, nullable = false)},
	inverseJoinColumns = {@JoinColumn(name = "status_id", referencedColumnName = "status_id", unique = false, nullable = false)})*/
	private UserStatusLookup userStatusLookup;
	
	@PrePersist
    public void onPrePersist() throws Exception {
		SimpleDateFormat ori_sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat target_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (this.created_datetime == null || this.created_datetime.isBlank()) {
            this.created_datetime = target_sdf.format(ori_sdf.parse(LocalDateTime.now().toString()));
        } if (this.modified_datetime == null || this.modified_datetime.isBlank()) {
            this.modified_datetime = target_sdf.format(ori_sdf.parse(LocalDateTime.now().toString()));
        }
    }
}
