package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando se modifica la cantidad de un ítem ya comandado (ej: aumento/disminución).
 */
public record ItemModifiedEvent(
        Long orderId,
        Long itemId,
        Integer oldQuantity,
        Integer newQuantity,
        String executorUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public ItemModifiedEvent(Long orderId, Long itemId, Integer oldQuantity, Integer newQuantity, String executorUsername) {
        this(orderId, itemId, oldQuantity, newQuantity, executorUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ItemModified";
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
                "oldQuantity", oldQuantity,
                "newQuantity", newQuantity
        );
    }
}
