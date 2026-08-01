package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO para representar una transacción de puntos.
 */
public record PointsTransactionResource(
        Long id,
        Long customerId,
        String type,
        int amount,
        Long saleId,
        String date
) {}
