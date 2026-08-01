package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido automáticamente cuando todos los ítems de un pedido pasan al estado DELIVERED.
 */
public record AllItemsDeliveredEvent(
        Long orderId,
        LocalDateTime timestamp
) implements DomainEvent {

    public AllItemsDeliveredEvent(Long orderId) {
        this(orderId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "AllItemsDelivered";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return "system";
    }

    @Override
    public Object payload() {
        return Map.of(
                "orderId", orderId
        );
    }
}
