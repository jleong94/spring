package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.modal.UserStatusLookup;

@Repository
public interface UserStatusLookupRepo extends CrudRepository<UserStatusLookup, Long> {

}
