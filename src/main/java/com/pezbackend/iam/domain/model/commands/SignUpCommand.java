package com.pezbackend.iam.domain.model.commands;

import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

/**
 * Comando para registrar un nuevo usuario en el sistema.
 *
 */
public record SignUpCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        Roles requestedRole
) {
    public SignUpCommand {
        if (email == null || email.isBlank())
            throw new BadRequestException("Email cannot be empty.");

        if (password == null || password.isBlank())
            throw new BadRequestException("Password cannot be empty.");

        if (firstName == null || firstName.isBlank())
            throw new BadRequestException("First name cannot be empty.");

        if (lastName == null || lastName.isBlank())
            throw new BadRequestException("Last name cannot be empty.");

        if (requestedRole == null)
            throw new BadRequestException("Requested role cannot be null.");
    }
}
