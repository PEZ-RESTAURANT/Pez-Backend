package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando los clientes sentados en una mesa solicitan la atención del mozo.
 */
public record TableAttentionRequestedEvent(
        Long tableId,
        Integer tableNumber,
        LocalDateTime timestamp
) implements DomainEvent {

    public TableAttentionRequestedEvent(Long tableId, Integer tableNumber) {
        this(tableId, tableNumber, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "TableAttentionRequested";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return "customer";
    }

    @Override
    public Object payload() {
        return Map.of(
                "tableId", tableId,
                "tableNumber", tableNumber
        );
    }
}
