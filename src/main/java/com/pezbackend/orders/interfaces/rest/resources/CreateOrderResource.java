package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Recurso DTO para iniciar una nueva comanda.
 */
public record CreateOrderResource(
        Long tableId,
        
        @NotBlank(message = "El tipo de pedido (DINE_IN, TAKEAWAY, DELIVERY) es obligatorio.")
        String type,
        
        Long customerId,
        
        String deliveryCustomerName,
        String deliveryCustomerPhone,
        String deliveryAddress,
        String deliveryMapsLink,
        String declaredPaymentMethod,
        Boolean ignoreDuplicatePhone
) {}
