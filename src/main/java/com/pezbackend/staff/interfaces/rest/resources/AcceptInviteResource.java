package com.pezbackend.staff.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteResource(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password
) {}
