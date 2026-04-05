package com.pezbackend.iam.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.NotFoundException;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
}
