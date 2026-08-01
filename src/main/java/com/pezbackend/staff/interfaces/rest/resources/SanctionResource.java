package com.pezbackend.staff.interfaces.rest.resources;

/**
 * Recurso DTO para representar una sanción.
 */
public record SanctionResource(
        Long id,
        Long staffProfileId,
        String type,
        String reason,
        String registeredBy,
        String date
) {}
