package com.repo.onboard;

import org.springframework.stereotype.Repository;

import com.modal.onboard.Merchant;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface MerchantRepo extends CrudRepository<Merchant, Long> {

	@Query(value = "SELECT 1 FROM merchant WHERE merchant_id = :merchant_id", nativeQuery = true)
    public Integer isMerchantIdExisted(@Param("merchant_id") String merchant_id);
	
	@Query(value = "CALL getMerchantDetailByMerchant_Id(:merchant_id);", nativeQuery = true)
    public Optional<Merchant> getMerchantDetailByMerchant_Id(@Param("merchant_id") String merchant_id);
}
