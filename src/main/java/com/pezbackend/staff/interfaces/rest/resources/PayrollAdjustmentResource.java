package com.pezbackend.staff.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para representar un ajuste de nómina.
 */
public record PayrollAdjustmentResource(
        Long id,
        Long staffProfileId,
        String type,
        BigDecimal amount,
        Long saleId,
        String registeredBy,
        String date
) {}
