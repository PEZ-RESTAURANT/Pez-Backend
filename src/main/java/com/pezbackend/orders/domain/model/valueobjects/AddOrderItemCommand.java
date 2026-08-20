package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Comando de dominio para agregar un plato a la comanda.
 */
public record AddOrderItemCommand(
        Long productId,
        Integer quantity,
        String note,
        Long waiterId
) {}
