package com.pezbackend.inventory.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para recibir la entrada de datos para un ajuste manual de stock.
 */
public record AdjustManualResource(
        BigDecimal quantity,
        String reason
) {}
