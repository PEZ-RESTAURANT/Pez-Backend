package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando un mozo atiende una mesa que estaba esperando en la cola.
 */
public record TableAttendedEvent(
        Long tableId,
        Integer tableNumber,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public TableAttendedEvent(Long tableId, Integer tableNumber, String waiterUsername) {
        this(tableId, tableNumber, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "TableAttended";
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
                "tableId", tableId,
                "tableNumber", tableNumber
        );
    }
}
