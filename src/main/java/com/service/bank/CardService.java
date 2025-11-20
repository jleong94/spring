package com.service.bank;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.enums.CardType;
import com.pojo.CardValidity;

@Service
public class CardService {
	
	// Regex pattern for basic card number format validation
    private final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    
    // Maximum allowed length for any card number
    private final int MAX_CARD_LENGTH = 19;
    
    // Minimum allowed length for any card number
    private final int MIN_CARD_LENGTH = 13;
	
	/**
     * Validates a credit card number using the Luhn algorithm and additional checks.
     * 
     * @param cardNumber The card number to validate 
     * @param aid AID from terminal
     * @return ValidationResult containing the result and any error messages
     */
	public CardValidity validateCard(Logger log, String cardNumber, String aid) {
        try {
            // Log validation attempt with masked card number
        	log.info("Check {} for card validity.", maskCardNumber(cardNumber));
            
            // Perform initial validation checks
        	CardValidity initialCheck = initialValidation(cardNumber);
            if (!initialCheck.isValid()) {
                return initialCheck;
            }

            // Clean the card number (remove spaces and special characters)
            String cleanedNumber = cleanCardNumber(cardNumber);
            
            // Validate card type and length
            CardType cardType = determineCardType(cleanedNumber, aid);
            if (cardType == CardType.UNKNOWN) {
            	return CardValidity.builder()
         			   .valid(false)
         			   .message("Unsupported card type.")
         			   .build();
            }
            
            // Perform Luhn algorithm check
            boolean luhnValid = luhnCheck(cleanedNumber);
            
            return CardValidity.builder()
      			   .valid(luhnValid)
      			   .message(!luhnValid ? "Invalid Card Number (Luhn Algorithm)." : "")
      			   .build();     
        } catch (Throwable e) {
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
			return CardValidity.builder()
	      			   .valid(false)
	      			   .message(e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Exception error.")
	      			   .build();
        }
    }

	/**
    * Performs initial validation checks on the card number.
    * 
    * @param cardNumber Raw card number input
    * @return ValidationResult with initial check results
    */
   private CardValidity initialValidation(String cardNumber) {
       if (cardNumber == null || cardNumber.trim().isEmpty()) {
    	   return CardValidity.builder()
    			   .valid(false)
    			   .message("Card number is blank.")
    			   .build();
       }
       
       String cleaned = cleanCardNumber(cardNumber);
       
       if (cleaned.length() > MAX_CARD_LENGTH || cleaned.length() < MIN_CARD_LENGTH) {
    	   return CardValidity.builder()
    			   .valid(false)
    			   .message("Invalid card number length.")
    			   .build();
       }
       
       if (!NUMERIC_PATTERN.matcher(cleaned).matches()) {
    	   return CardValidity.builder()
    			   .valid(false)
    			   .message("Card number must contain only digits.")
    			   .build();
       }
       
       return CardValidity.builder()
			   .valid(true)
			   .message("Initial validation passed.")
			   .build();
   }
	
	/**
     * Performs the Luhn algorithm check on the card number.
     * 
     * @param cardNumber Cleaned card number
     * @return true if the card number passes the Luhn check
     */
    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        // Process from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
	
	/**
     * Removes all non-digit characters from the card number.
     * 
     * @param cardNumber Raw card number input
     * @return Cleaned card number containing only digits
     */
    private String cleanCardNumber(String cardNumber) {
        return cardNumber.replaceAll("[^0-9]", "");
    }
	
	/**
     * Determines the card type based on the card number prefix.
     * 
     * @param cardNumber Cleaned card number
     * @param aid AID from terminal
     * @return CardType enum representing the detected card type
     */
    private CardType determineCardType(String cardNumber, String aid) {
    	// VISA: Starts with 4, length 16
        if (Pattern.matches("^4[0-9]{15}$", cardNumber)) {
        	if(aid != null && aid.trim().equalsIgnoreCase("A0000005241010")) {
        		return CardType.MYDEBIT;
        	}
            return CardType.VISA;
        }
        // MASTERCARD: Starts with 51-55, length 16
        if (Pattern.matches("^5[1-5][0-9]{14}$", cardNumber)) {
        	if(aid != null && aid.trim().equalsIgnoreCase("A0000005241010")) {
        		return CardType.MYDEBIT;
        	}
            return CardType.MASTERCARD;
        }
        // AMEX: Starts with 34 or 37, length 15
        if (Pattern.matches("^3[47][0-9]{13}$", cardNumber)) {
            return CardType.AMEX;
        }
        // DISCOVER: Starts with 6011 or 65, length 16
        if (Pattern.matches("^(6011|65)[0-9]{12}$", cardNumber)) {
            return CardType.DISCOVER;
        }
        // UNIONPAY: Starts with 62, length 16-19
        if (Pattern.matches("^62[0-9]{14,17}$", cardNumber)) {
            return CardType.UNIONPAY;
        }
        // JCB: Starts with 3528-3589, length 16
        if (Pattern.matches("^(352[8-9]|35[3-8][0-9]|3589)[0-9]{12}$", cardNumber)) {
            return CardType.JCB;
        }
        return CardType.UNKNOWN;
    }

	/**
     * Masks the card number for logging purposes.
     * 
     * @param cardNumber The card number to mask
     * @return Masked card number showing only last 4 digits
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() <= 4) {
            return "****";
        }
        return "*".repeat(cardNumber.length() - 4).concat(cardNumber.substring(cardNumber.length() - 4));
    }
}
