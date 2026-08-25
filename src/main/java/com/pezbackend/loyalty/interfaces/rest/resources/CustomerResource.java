package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO para representar a un cliente afiliado.
 */
public record CustomerResource(
        Long id,
        String phone,
        String fullName,
        String email,
        String birthday,
        String address,
        String documentNumber,
        String lastPaymentMethod,
        boolean affiliated,
        boolean dataConsentAccepted,
        String dataConsentDate,
        int pointsBalance
) {}
