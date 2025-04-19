package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.modal.UserRoleLookup;

@Repository
public interface UserRoleLookupRepo extends CrudRepository<UserRoleLookup, Long> {

}
