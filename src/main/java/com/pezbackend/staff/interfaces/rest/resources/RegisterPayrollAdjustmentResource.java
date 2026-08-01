package com.pezbackend.staff.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para registrar un ajuste de nómina.
 */
public record RegisterPayrollAdjustmentResource(
        String type,
        BigDecimal amount,
        Long saleId,
        String date
) {}
