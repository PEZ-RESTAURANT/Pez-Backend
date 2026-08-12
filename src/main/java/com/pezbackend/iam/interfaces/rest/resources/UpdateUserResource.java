package com.pezbackend.iam.interfaces.rest.resources;

import com.pezbackend.iam.domain.model.valueobjects.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Recurso DTO para recibir datos de actualización de usuario
 */
public record UpdateUserResource(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName,

    @NotNull(message = "Requested role is required")
    Roles requestedRole,

    @NotNull(message = "Active status is required")
    Boolean active
) {
}
