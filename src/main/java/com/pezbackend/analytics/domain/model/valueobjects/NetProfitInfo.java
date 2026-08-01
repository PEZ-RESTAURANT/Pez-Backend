package com.pezbackend.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Record que representa la ganancia neta consolidada para un período.
 */
public record NetProfitInfo(
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        boolean isApproximation
) {}
