package com.pezbackend.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Record que representa el rendimiento de una zona de cocina.
 */
public record KitchenZonePerformanceInfo(
        String zoneName,
        long itemsCount,
        BigDecimal revenue
) {}
