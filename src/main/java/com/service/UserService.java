package com.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.enums.ResponseCode;
import com.modal.EMail;
import com.modal.Permission;
import com.modal.User;
import com.modal.UserActionLookup;
import com.modal.UserActionPermission;
import com.pojo.ApiResponse;
import com.pojo.Property;
import com.repo.PermissionRepo;
import com.repo.UserActionLookupRepo;
import com.repo.UserActionPermissionRepo;
import com.repo.UserRepo;
import com.repo.UserRoleLookupRepo;
import com.repo.UserStatusLookupRepo;
import com.utilities.Tool;

@Service
public class UserService {

	@Autowired
	Tool tool;

	@Autowired
	UserRepo userRepo;
	
	@Autowired
	UserActionPermissionRepo userActionPermissionRepo;

	@Autowired
	Property property;

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

	public ResponseEntity<ApiResponse> userRegistration(User user){
		long count = userRepo.count();
		long role_id = count <= 0 ? 1L : 2L;
		String password = tool.generatePassword(8);
		user = user.toBuilder()
		.password(password)
		.userStatusLookup(userStatusLookupRepo.findById(1L).orElseThrow(() -> new RuntimeException("Default user registration status not found.")))
		.userRoleLookup(userRoleLookupRepo.findById(role_id).orElseThrow(() -> new RuntimeException("Default user registration role not found.")))
		.build();
		user = userRepo.save(user);
		
		List<UserActionPermission> userActionPermission = new ArrayList<UserActionPermission>();
		List<UserActionLookup> userActionLookups = userActionLookupRepo.getUniqueAllUserActionLookupList().orElseThrow(() -> new RuntimeException("Default user action lookup list not found."));
		List<Permission> permissions = permissionRepo.getUniqueAllPermissionLookupList().orElseThrow(() -> new RuntimeException("Default user permission lookup list not found."));		
		for(UserActionLookup userActionLookup : userActionLookups) {
			for(Permission permission : permissions) {
				userActionPermission.add(UserActionPermission.builder()
						.user(user)
						.userActionLookup(userActionLookup)
						.permission(permission)
						.build());
				if(role_id != 1L) {break;}
			}
		}
		userActionPermissionRepo.saveAll(userActionPermission);
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
		String password = tool.generatePassword(8);
		user.setPassword(password);
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
