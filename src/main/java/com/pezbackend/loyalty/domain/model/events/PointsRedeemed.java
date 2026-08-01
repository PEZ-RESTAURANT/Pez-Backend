package com.pezbackend.loyalty.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando un cliente canjea sus puntos acumulados.
 */
public record PointsRedeemed(
        Long customerId,
        int points,
        LocalDateTime timestamp
) implements DomainEvent {

    public PointsRedeemed(Long customerId, int points) {
        this(customerId, points, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "PointsRedeemed";
    }

    @Override
    public String module() {
        return "loyalty";
    }

    @Override
    public String userId() {
        return "system";
    }

    @Override
    public Object payload() {
        return Map.of(
                "customerId", customerId,
                "points", points
        );
    }
}
