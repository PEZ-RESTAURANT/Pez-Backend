package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al cancelar una reserva.
 */
public record ReservationCancelledEvent(
        Long reservationId,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public ReservationCancelledEvent(Long reservationId, String waiterUsername) {
        this(reservationId, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ReservationCancelled";
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
                "reservationId", reservationId
        );
    }
}
