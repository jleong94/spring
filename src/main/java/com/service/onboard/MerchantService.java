package com.service.onboard;

import java.util.Random;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.modal.onboard.Merchant;
import com.pojo.ApiResponse;
import com.repo.UserStatusLookupRepo;
import com.repo.onboard.MerchantRepo;
import com.utilities.Tool;

@Service
public class MerchantService {
	
	@Autowired
	Tool tool;
	
	@Autowired
	MerchantRepo merchantRepo;
	
	@Autowired
	UserStatusLookupRepo userStatusLookupRepo;

	public String generateMerchantId(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        String result = "";
        do {
        	result += characters.charAt(random.nextInt(characters.length()));
        } while(result.length() < length);
        return result;
    }
	
	@Transactional(
			propagation = Propagation.REQUIRED, 
			isolation = Isolation.READ_COMMITTED, 
			timeout = 1, 
			readOnly = false, 
			rollbackFor = Exception.class
			)
	public ApiResponse registerMerchant(Logger log, Merchant merchant) {
		try {
			Integer isExisted;
			do {
				merchant.setMerchant_id(generateMerchantId(15));
				isExisted = merchantRepo.isMerchantIdExisted(merchant.getMerchant_id());
			} while(isExisted != null && isExisted == 1);
			merchant.setUserStatusLookup(userStatusLookupRepo.findById(1L).orElseThrow(() -> new RuntimeException("Error occured(Default status not found).")));
			merchant = merchantRepo.save(merchant);
			if(merchant.getId() > 0) {
				return new ApiResponse(0, tool.getTodayDateTimeInString(log), merchant);
			} else {
				return new ApiResponse(3, tool.getTodayDateTimeInString(log));
			}
		} catch(Exception e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			return new ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log));
		}
		
	}
	
	@Transactional(
			propagation = Propagation.REQUIRED, 
			isolation = Isolation.READ_COMMITTED, 
			timeout = 1, 
			readOnly = false, 
			rollbackFor = Exception.class
			)
	public ApiResponse getMerchantByMerchant_Id(Logger log, String merchant_id) {
		try {
			Merchant merchant = merchantRepo.getMerchantDetailByMerchant_Id(merchant_id)
					.orElseThrow(() -> new RuntimeException("Merchant not found."));
			return new ApiResponse(0, tool.getTodayDateTimeInString(log), merchant);
		} catch(Exception e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			return new ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log));
		}
		
	}
	
	@Transactional(
			propagation = Propagation.REQUIRED, 
			isolation = Isolation.READ_COMMITTED, 
			timeout = 1, 
			readOnly = false, 
			rollbackFor = Exception.class
			)
	public ApiResponse updMerchantByMerchant_Id(Logger log, Merchant merchant, String merchant_id) {
		try {
			Merchant curr_merchant = merchantRepo.getMerchantDetailByMerchant_Id(merchant_id)
			.orElseThrow(() -> new RuntimeException("Merchant not found."));
			tool.mergeObj(merchant, curr_merchant);
			merchant.setModified_datetime(tool.getTodayDateTimeInString(log));
			merchant = merchantRepo.save(merchant);
			return new ApiResponse(0, tool.getTodayDateTimeInString(log), merchant);
		} catch(Exception e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			return new ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log));
		}
		
	}
	
	@Transactional(
			propagation = Propagation.REQUIRED, 
			isolation = Isolation.READ_COMMITTED, 
			timeout = 1, 
			readOnly = false, 
			rollbackFor = Exception.class
			)
	public ApiResponse dltMerchantByMerchant_Id(Logger log, String merchant_id) {
		try {
			Merchant merchant = merchantRepo.getMerchantDetailByMerchant_Id(merchant_id)
			.orElseThrow(() -> new RuntimeException("Merchant not found."));
			merchantRepo.delete(merchant);
			return new ApiResponse(0, tool.getTodayDateTimeInString(log), merchant);
		} catch(Exception e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			return new ApiResponse(-2, e.getMessage(), tool.getTodayDateTimeInString(log));
		}
		
	}
}
