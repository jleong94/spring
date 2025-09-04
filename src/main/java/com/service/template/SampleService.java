package com.service.template;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

@Service
public class SampleService {

	private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
	
	public String generateRandomIc() {
		// Format: XXXXX-XX-XXXX
		return String.format("%06d-%02d-%04d",
				threadLocalRandom.nextInt(900) + 100,  
				threadLocalRandom.nextInt(100),        
				threadLocalRandom.nextInt(10000));     
	}
	
	public String generatePassword(int length) {
		String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	    String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    String DIGITS = "0123456789";
	    String SPECIAL_CHARS = "!@#$%^&*()-_+=<>?";
	    
        String allChars = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARS;
        
        return ThreadLocalRandom.current()
            .ints(length, 0, allChars.length())
            .mapToObj(allChars::charAt)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }
}
