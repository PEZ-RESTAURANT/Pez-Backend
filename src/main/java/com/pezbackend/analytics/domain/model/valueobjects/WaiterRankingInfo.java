package com.pezbackend.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Record que representa el ranking de ventas de un mozo.
 */
public record WaiterRankingInfo(
        Long waiterId,
        String firstName,
        String lastName,
        BigDecimal totalSales
) {}
