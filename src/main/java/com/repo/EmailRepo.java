package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.modal.Email;

@Repository
public interface EmailRepo extends CrudRepository<Email, Long> {

}
