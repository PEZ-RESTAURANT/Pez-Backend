package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando se agrega un nuevo plato a una comanda.
 */
public record ItemOrderedEvent(
        Long orderId,
        Long itemId,
        Long productId,
        Integer quantity,
        Long waiterId,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public ItemOrderedEvent(Long orderId, Long itemId, Long productId, Integer quantity, Long waiterId, String waiterUsername) {
        this(orderId, itemId, productId, quantity, waiterId, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ItemOrdered";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return waiterUsername;
    }

    @Override
    public Object payload() {
        return Map.of(
                "orderId", orderId,
                "itemId", itemId,
                "productId", productId,
                "quantity", quantity,
                "waiterId", waiterId
        );
    }
}
