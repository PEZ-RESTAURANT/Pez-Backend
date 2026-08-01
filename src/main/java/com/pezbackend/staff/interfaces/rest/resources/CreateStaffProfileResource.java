package com.pezbackend.staff.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para crear un perfil de personal.
 */
public record CreateStaffProfileResource(
        Long accountId,
        String paymentType,
        BigDecimal agreedAmount
) {}
