package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al registrar una nueva reserva.
 */
public record ReservationCreatedEvent(
        Long reservationId,
        String customerName,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public ReservationCreatedEvent(Long reservationId, String customerName, String waiterUsername) {
        this(reservationId, customerName, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ReservationCreated";
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
                "reservationId", reservationId,
                "customerName", customerName
        );
    }
}
