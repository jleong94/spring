package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.modal.Permission;

@Repository
public interface PermissionRepo extends CrudRepository<Permission, Long> {

}
