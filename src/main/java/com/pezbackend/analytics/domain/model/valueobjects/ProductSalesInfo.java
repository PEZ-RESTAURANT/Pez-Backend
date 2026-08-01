package com.pezbackend.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Record que representa la cantidad y recaudación de ventas de un producto, con alerta de ventas bajas.
 */
public record ProductSalesInfo(
        String productName,
        long quantitySold,
        BigDecimal totalRevenue,
        boolean lowSalesAlert
) {}
