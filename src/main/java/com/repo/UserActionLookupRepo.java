package com.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.modal.UserActionLookup;

@Repository
public interface UserActionLookupRepo extends CrudRepository<UserActionLookup, Long> {

	@Query(value = "SELECT DISTINCT user_action_id, action_name FROM user_action_lookup ORDER BY user_action_id", nativeQuery = true)
    public Optional<List<UserActionLookup>> getUniqueAllUserActionLookupList();
}
