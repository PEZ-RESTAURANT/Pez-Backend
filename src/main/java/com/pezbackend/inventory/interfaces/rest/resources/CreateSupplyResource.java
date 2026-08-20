package com.pezbackend.inventory.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para recibir la entrada de datos al registrar un nuevo insumo.
 */
public record CreateSupplyResource(
        String name,
        String unit,
        BigDecimal minThreshold,
        BigDecimal criticalThreshold
) {}
