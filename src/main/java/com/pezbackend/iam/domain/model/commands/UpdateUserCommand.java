package com.pezbackend.iam.domain.model.commands;

import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

/**
 * Comando para actualizar la cuenta de un trabajador.
 */
public record UpdateUserCommand(
        Long id,
        String email,
        String firstName,
        String lastName,
        Roles requestedRole,
        Boolean active
) {
    public UpdateUserCommand {
        if (id == null)
            throw new BadRequestException("User ID cannot be null.");

        if (email == null || email.isBlank())
            throw new BadRequestException("Email cannot be empty.");

        if (firstName == null || firstName.isBlank())
            throw new BadRequestException("First name cannot be empty.");

        if (lastName == null || lastName.isBlank())
            throw new BadRequestException("Last name cannot be empty.");

        if (requestedRole == null)
            throw new BadRequestException("Requested role cannot be null.");

        if (active == null)
            throw new BadRequestException("Active status cannot be null.");
    }
}
