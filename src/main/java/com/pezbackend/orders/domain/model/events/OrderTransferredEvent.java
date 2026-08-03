package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al trasladar una comanda de una mesa a otra.
 */
public record OrderTransferredEvent(
        Long orderId,
        Long fromTableId,
        Long toTableId,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public OrderTransferredEvent(Long orderId, Long fromTableId, Long toTableId, String waiterUsername) {
        this(orderId, fromTableId, toTableId, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "OrderTransferred";
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
                "fromTableId", fromTableId,
                "toTableId", toTableId
        );
    }
}
