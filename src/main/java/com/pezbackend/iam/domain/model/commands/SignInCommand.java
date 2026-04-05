package com.pezbackend.iam.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

/**
 * Command to sign in a user in the system.
 */
public record SignInCommand(
        String email,
        String password
) {
    public SignInCommand {
        if (email == null || email.isBlank())
            throw new BadRequestException("Email cannot be empty.");

        if (password == null || password.isBlank())
            throw new BadRequestException("Password cannot be empty.");
    }
}