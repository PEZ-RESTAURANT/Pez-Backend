package com.pezbackend.orders.interfaces.rest.resources;

import com.pezbackend.orders.domain.model.valueobjects.OrderStatus;
import com.pezbackend.orders.domain.model.valueobjects.OrderType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Recurso DTO principal para exponer una comanda completa con sus ítems y ajustes.
 */
public record OrderResource(
        Long id,
        Long tableId,
        OrderType type,
        Long customerId,
        OrderStatus status,
        LocalDateTime attendedAt,
        LocalDateTime createdAt,
        List<OrderItemResource> items,
        List<PriceAdjustmentResource> priceAdjustments,
        String deliveryCustomerName,
        String deliveryCustomerPhone,
        String deliveryAddress,
        String deliveryMapsLink,
        String declaredPaymentMethod
) {}
