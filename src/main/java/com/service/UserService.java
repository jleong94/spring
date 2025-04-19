package com.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.configuration.UserInfoDetails;
import com.modal.User;
import com.repo.UserRepo;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepo userRepo;

	/*
	 * This function will auto call by spring security or application logic during authentication/authorization
	 * */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepo.findByUsername(username);
		return user.map(UserInfoDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
