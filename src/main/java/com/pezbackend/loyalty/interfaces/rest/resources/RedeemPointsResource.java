package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO para el canje de puntos.
 */
public record RedeemPointsResource(
        int points,
        String date
) {}
