package com.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.modal.User;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {
	
	@Query(value = "SELECT * FROM user WHERE username = :username", nativeQuery = true)
    public Optional<User> findByUsername(@Param("username") String username);
	
	@Query(value = "SELECT * FROM user WHERE username = :username AND email = :email", nativeQuery = true)
    public Optional<User> findByUsernameAndEmail(@Param("username") String username, @Param("email") String email);
}
