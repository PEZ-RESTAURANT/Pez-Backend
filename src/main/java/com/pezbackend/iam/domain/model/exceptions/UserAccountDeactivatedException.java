package com.pezbackend.iam.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

/**
 * Exception thrown when attempting to authenticate with a deactivated user account
 */
public class UserAccountDeactivatedException extends BusinessRuleException {

    public UserAccountDeactivatedException(String email) {
        super("User account with email " + email + " is deactivated");
    }

    public UserAccountDeactivatedException(Long userId) {
        super("User account with ID " + userId + " is deactivated");
    }
}
