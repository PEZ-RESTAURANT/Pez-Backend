package com.pezbackend.staff.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para registrar horas extras.
 */
public record RegisterOvertimeResource(
        BigDecimal hours,
        String date
) {}
