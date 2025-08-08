package com.exception;

public class UserNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * Constructor for RateLimitExceededException
     * @param message The error message to be passed to the parent RuntimeException
     */
    public UserNotFoundException(String message) {
        super(message);
    }

}
