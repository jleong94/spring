package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import com.configuration.UserInfoDetails;
import com.enums.ResponseCode;
import com.modal.EMail;
import com.modal.Permission;
import com.modal.User;
import com.modal.UserActionLookup;
import com.pojo.ApiResponse;
import com.properties.Property;
import com.repo.PermissionRepo;
import com.repo.UserActionLookupRepo;
import com.repo.UserRepo;
import com.repo.UserRoleLookupRepo;
import com.repo.UserStatusLookupRepo;
import com.utilities.Tool;

import io.jsonwebtoken.lang.Collections;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	Property property;

	@Autowired
	Tool tool;

	@Autowired
	JwtService jwtService;

	@Autowired
	UserStatusLookupRepo userStatusLookupRepo;

	@Autowired
	UserRoleLookupRepo userRoleLookupRepo;

	@Autowired
	UserActionLookupRepo userActionLookupRepo;

	@Autowired
	PermissionRepo permissionRepo;
	
	@Autowired
	EMailService emailService;

	/*
	 * This function will auto call by spring security or application logic during authentication/authorization
	 * */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByUsername(username)
				.map(x -> new UserInfoDetails(x))
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

	public ResponseEntity<ApiResponse> userRegistration(User user){
		long count = userRepo.count();
		long role_id = count <= 0 ? 1L : 2L;
		
		List<UserActionLookup> userActionLookups = userActionLookupRepo.getUniqueAllUserActionLookupList();
		List<Permission> permissions = permissionRepo.getUniqueAllPermissionLookupList();
		for(UserActionLookup userActionLookup : userActionLookups) {
			List<Permission> new_permission = Collections.emptyList();
			for(Permission permission : permissions) {
				new_permission.add(permission);
				if(role_id != 1L) {break;}
			}
			userActionLookup.setPermission(new_permission);
		}
		user = User.builder()
				.password(new Argon2PasswordEncoder(16, 32, 1, 65536, 10).encode(user.getPassword()))
				.jwt_token_expiration(property.getJwt_token_expiration())
				.jwt_token_secret_key(jwtService.generateSecretKey())
				.userStatusLookup(userStatusLookupRepo.findById(1L).orElseThrow(() -> new RuntimeException("Default user registration status not found.")))
				.userRoleLookup(userRoleLookupRepo.findById(role_id).orElseThrow(() -> new RuntimeException("Default user registration role not found.")))
				.userActionLookup(userActionLookups)
				.build();
		user = userRepo.save(user);
		if(user.getId() > 0L) {
			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.SUCCESS.getResponse_code())
					.resp_msg(ResponseCode.SUCCESS.getResponse_status())
					.datetime(tool.getTodayDateTimeInString())
					.user(user)
					.build());
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ApiResponse
					.builder()
					.resp_code(ResponseCode.FAILED.getResponse_code())
					.resp_msg("Registration failed.")
					.datetime(tool.getTodayDateTimeInString())
					.build());
		}
	}
	
	public ResponseEntity<ApiResponse> resetPassword(User user) throws Exception{
		String username = user.getUsername();
		user = userRepo.findByUsernameAndEmail(user.getUsername(), user.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		String password = tool.generatePassword(10);
		user.setPassword(new Argon2PasswordEncoder(16, 32, 1, 65536, 10).encode(password));
		userRepo.save(user);
		emailService.sendEMail(EMail.builder()
				.receiver(user.getEmail())
				.subject("Reset Password")
				.body("Your new password for username, "
						.concat(user.getUsername())
						.concat(" is ")
						.concat(password)
						.concat("."))
				.build());
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse
				.builder()
				.resp_code(ResponseCode.SUCCESS.getResponse_code())
				.resp_msg(ResponseCode.SUCCESS.getResponse_status())
				.datetime(tool.getTodayDateTimeInString())
				.build());
	}
}
