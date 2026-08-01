package com.pezbackend.inventory.interfaces.rest.resources;

import com.pezbackend.inventory.domain.model.valueobjects.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recurso DTO que representa los detalles expuestos de un movimiento de stock.
 */
public record StockMovementResource(
        Long id,
        Long supplyId,
        StockMovementType type,
        BigDecimal quantity,
        String registeredBy,
        LocalDateTime date,
        String reason
) {}
