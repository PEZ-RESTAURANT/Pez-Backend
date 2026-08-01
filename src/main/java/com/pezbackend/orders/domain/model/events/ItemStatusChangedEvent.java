package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al cambiar el estado de preparación o entrega de un plato (ej: PENDING -> READY, READY -> DELIVERED).
 */
public record ItemStatusChangedEvent(
        Long orderId,
        Long itemId,
        String oldStatus,
        String newStatus,
        String executorUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public ItemStatusChangedEvent(Long orderId, Long itemId, String oldStatus, String newStatus, String executorUsername) {
        this(orderId, itemId, oldStatus, newStatus, executorUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ItemStatusChanged";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return executorUsername;
    }

    @Override
    public Object payload() {
        return Map.of(
                "orderId", orderId,
                "itemId", itemId,
                "oldStatus", oldStatus,
                "newStatus", newStatus
        );
    }
}
