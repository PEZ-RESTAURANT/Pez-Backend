package com.pezbackend.inventory.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para recibir la entrada de datos al actualizar un insumo existente.
 */
public record UpdateSupplyResource(
        String name,
        String unit,
        BigDecimal minThreshold
) {}
