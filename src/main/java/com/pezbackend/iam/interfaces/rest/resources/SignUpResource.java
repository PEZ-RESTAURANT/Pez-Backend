package com.pezbackend.iam.interfaces.rest.resources;

import com.pezbackend.iam.domain.model.valueobjects.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Recurso para registro de usuario
 * Solo contiene información de autenticación (IAM).
 * Los datos del cliente (DNI, dirección, teléfono) se manejan en el bounded context Cliente.
 */
public record SignUpResource(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password,
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName,
    
    @NotNull(message = "Requested role is required")
    Roles requestedRole
) {
    
}
