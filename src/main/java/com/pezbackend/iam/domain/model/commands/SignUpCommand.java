package io.github.rafaviv.panther.pantherbackend.iam.domain.model.commands;

import io.github.rafaviv.panther.pantherbackend.iam.domain.model.valueobjects.Roles;

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
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty.");
        }
        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty.");
        }
        if (requestedRole == null) {
            throw new IllegalArgumentException("Requested role cannot be null.");
        }
    }
}
