package com.pezbackend.staff.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para representar el resumen de pagos y cobros de un empleado.
 */
public record PaymentSummaryResource(
        BigDecimal agreedAmount,
        BigDecimal totalAdvances,
        BigDecimal totalDeductions,
        BigDecimal totalOvertimeHours,
        BigDecimal netPending
) {}
