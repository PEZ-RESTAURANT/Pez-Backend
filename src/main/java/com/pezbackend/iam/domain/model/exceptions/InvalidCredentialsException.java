package com.pezbackend.iam.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.UnauthorizedException;

/**
 * Exception thrown when authentication credentials are invalid
 */
public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
