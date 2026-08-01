package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO para representar a un cliente afiliado.
 */
public record CustomerResource(
        Long id,
        String phone,
        String fullName,
        String birthday,
        String address,
        boolean dataConsentAccepted,
        String dataConsentDate,
        int pointsBalance
) {}
