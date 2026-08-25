package com.pezbackend.staff.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para representar el perfil de un empleado.
 */
public record StaffProfileResource(
        Long id,
        Long accountId,
        String paymentType,
        BigDecimal agreedAmount,
        BigDecimal overtimeHourlyRate,
        boolean fingerprintConsent,
        String fingerprintConsentDate,
        Integer fingerprintId
) {}
