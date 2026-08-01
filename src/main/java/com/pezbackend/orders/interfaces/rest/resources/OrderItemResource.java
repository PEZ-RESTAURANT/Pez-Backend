package com.pezbackend.orders.interfaces.rest.resources;

import com.pezbackend.orders.domain.model.valueobjects.OrderItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recurso DTO para exponer la información de un plato en una comanda.
 */
public record OrderItemResource(
        Long id,
        Long productId,
        Integer quantity,
        String note,
        Long waiterId,
        BigDecimal unitPriceSnapshot,
        OrderItemStatus status,
        LocalDateTime readyAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {}
