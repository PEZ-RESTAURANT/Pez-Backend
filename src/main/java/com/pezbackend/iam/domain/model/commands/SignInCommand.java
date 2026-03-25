package com.pezbackend.iam.domain.model.commands;

/**
 * Command to sign in a user in the system.
 */
public record SignInCommand(
    String email,
    String password
) {
    public SignInCommand {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
    }
}