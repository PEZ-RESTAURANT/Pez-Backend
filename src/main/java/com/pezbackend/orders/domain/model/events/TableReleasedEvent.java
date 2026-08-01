package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al liberar la mesa tras completarse el pago, dejándola en estado FREE.
 */
public record TableReleasedEvent(
        Long tableId,
        Integer tableNumber,
        LocalDateTime timestamp
) implements DomainEvent {

    public TableReleasedEvent(Long tableId, Integer tableNumber) {
        this(tableId, tableNumber, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "TableReleased";
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
                "tableId", tableId,
                "tableNumber", tableNumber
        );
    }
}
