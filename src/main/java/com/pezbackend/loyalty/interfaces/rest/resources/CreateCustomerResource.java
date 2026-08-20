package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO para crear/registrar un cliente en el programa de fidelización.
 */
public record CreateCustomerResource(
        String phone,
        String fullName,
        String email,
        String birthday,
        String address,
        boolean dataConsentAccepted
) {}
