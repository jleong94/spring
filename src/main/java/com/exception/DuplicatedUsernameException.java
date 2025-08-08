package com.exception;

public class DuplicatedUsernameException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * Constructor for RateLimitExceededException
     * @param message The error message to be passed to the parent RuntimeException
     */
    public DuplicatedUsernameException(String message) {
        super(message);
    }

}
