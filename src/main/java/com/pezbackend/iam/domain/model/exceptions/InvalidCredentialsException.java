package com.pezbackend.iam.domain.model.exceptions;

/**
 * Exception thrown when authentication credentials are invalid
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

