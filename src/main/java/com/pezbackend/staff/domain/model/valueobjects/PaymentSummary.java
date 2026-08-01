package com.pezbackend.staff.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Record que representa el resumen consolidado de pagos y descuentos de un empleado.
 */
public record PaymentSummary(
        BigDecimal agreedAmount,
        BigDecimal totalAdvances,
        BigDecimal totalDeductions,
        BigDecimal totalOvertimeHours,
        BigDecimal netPending
) {}
