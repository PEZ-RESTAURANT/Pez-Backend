package com.pezbackend.analytics.domain.model.valueobjects;

/**
 * Record que representa un par de productos vendidos juntos.
 */
public record ComboInfo(
        String productA,
        String productB,
        long count
) {}
