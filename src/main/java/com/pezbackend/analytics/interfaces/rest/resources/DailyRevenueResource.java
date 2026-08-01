package com.pezbackend.analytics.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso que expone el total de recaudación de un día.
 */
public record DailyRevenueResource(BigDecimal totalRevenue) {}
