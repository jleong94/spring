package com.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.modal.Permission;

@Repository
public interface PermissionRepo extends CrudRepository<Permission, Long> {

	@Query(value = "SELECT DISTINCT permission_id, permission_name FROM permission ORDER BY permission_id", nativeQuery = true)
    public Optional<List<Permission>> getUniqueAllPermissionLookupList();
}
