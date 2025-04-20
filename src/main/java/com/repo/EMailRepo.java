package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.modal.EMail;

@Repository
public interface EMailRepo extends CrudRepository<EMail, Long> {

}
