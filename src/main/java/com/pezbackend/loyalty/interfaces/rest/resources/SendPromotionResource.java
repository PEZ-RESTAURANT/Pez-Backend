package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO para recibir los parámetros de envío de una promoción manual.
 */
public record SendPromotionResource(
        String subject,
        String message
) {}
