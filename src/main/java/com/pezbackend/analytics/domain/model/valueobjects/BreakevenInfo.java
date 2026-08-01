package com.pezbackend.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Record que representa el punto de equilibrio de un período.
 */
public record BreakevenInfo(
        BigDecimal fixedExpenses,
        BigDecimal averageTicket,
        BigDecimal breakevenSalesCount,
        boolean isApproximation
) {}
