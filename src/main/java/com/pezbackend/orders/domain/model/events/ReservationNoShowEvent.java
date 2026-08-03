package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al registrar inasistencia a una reserva.
 */
public record ReservationNoShowEvent(
        Long reservationId,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public ReservationNoShowEvent(Long reservationId, String waiterUsername) {
        this(reservationId, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ReservationNoShow";
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
