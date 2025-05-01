package com.exception;

public class UnauthenticatedAccessException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructor for RateLimitExceededException
     * @param message The error message to be passed to the parent RuntimeException
     */
    public UnauthenticatedAccessException(String message) {
        super(message);
    }
}
