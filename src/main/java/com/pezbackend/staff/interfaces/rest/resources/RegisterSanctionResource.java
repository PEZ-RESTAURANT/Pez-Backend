package com.pezbackend.staff.interfaces.rest.resources;

/**
 * Recurso DTO para registrar una sanción.
 */
public record RegisterSanctionResource(
        String type,
        String reason,
        String date
) {}
