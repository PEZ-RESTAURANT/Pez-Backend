package com.pezbackend.staff.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record CreateStaffInviteResource(
    @NotBlank String requestedRole,
    @NotBlank String email
) {}
