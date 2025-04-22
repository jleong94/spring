package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.modal.UserActionLookup;

@Repository
public interface UserActionLookupRepo extends CrudRepository<UserActionLookup, Long> {

}
