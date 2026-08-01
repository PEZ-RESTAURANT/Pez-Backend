package com.pezbackend.loyalty.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando un cliente acumula puntos por compra.
 */
public record PointsEarned(
        Long customerId,
        int points,
        Long saleId,
        LocalDateTime timestamp
) implements DomainEvent {

    public PointsEarned(Long customerId, int points, Long saleId) {
        this(customerId, points, saleId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "PointsEarned";
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
                "points", points,
                "saleId", saleId != null ? saleId : -1
        );
    }
}
