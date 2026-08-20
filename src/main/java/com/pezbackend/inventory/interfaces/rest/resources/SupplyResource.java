package com.pezbackend.inventory.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO de salida que representa los datos expuestos de un insumo.
 */
public record SupplyResource(
        Long id,
        String name,
        String unit,
        BigDecimal currentStock,
        BigDecimal minThreshold,
        BigDecimal criticalThreshold,
        String stockLevel
) {}
