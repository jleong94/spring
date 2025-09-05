package com.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.modal.AuditLog;

@Repository
public interface AuditLogRepo extends CrudRepository<AuditLog, Long> {

}
