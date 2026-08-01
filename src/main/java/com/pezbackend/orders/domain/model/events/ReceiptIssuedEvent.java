package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al generar la precuenta y cambiar el estado del pedido a ISSUED_UNPAID.
 */
public record ReceiptIssuedEvent(
        Long orderId,
        String issuedBy,
        LocalDateTime timestamp
) implements DomainEvent {

    public ReceiptIssuedEvent(Long orderId, String issuedBy) {
        this(orderId, issuedBy, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ReceiptIssued";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return issuedBy;
    }

    @Override
    public Object payload() {
        return Map.of(
                "orderId", orderId
        );
    }
}
