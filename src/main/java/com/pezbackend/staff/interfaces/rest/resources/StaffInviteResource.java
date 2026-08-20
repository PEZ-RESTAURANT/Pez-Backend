package com.pezbackend.staff.interfaces.rest.resources;

public record StaffInviteResource(
    Long id,
    String code,
    String requestedRole,
    String email,
    String expiresAt,
    boolean used,
    boolean revoked,
    boolean expired,
    Long restaurantId
) {}
