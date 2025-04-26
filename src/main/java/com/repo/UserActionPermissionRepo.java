package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.modal.UserActionPermission;

@Repository
public interface UserActionPermissionRepo extends CrudRepository<UserActionPermission, Long> {

}
