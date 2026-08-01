package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al aplicar un ajuste o descuento en el precio de la comanda.
 */
public record PriceAdjustmentAppliedEvent(
        Long orderId,
        String scope,
        String validity,
        BigDecimal newValue,
        String reason,
        String appliedBy,
        LocalDateTime timestamp
) implements DomainEvent {

    public PriceAdjustmentAppliedEvent(Long orderId, String scope, String validity, BigDecimal newValue, String reason, String appliedBy) {
        this(orderId, scope, validity, newValue, reason, appliedBy, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "PriceAdjustmentApplied";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return appliedBy;
    }

    @Override
    public Object payload() {
        return Map.of(
                "orderId", orderId,
                "scope", scope,
                "validity", validity,
                "newValue", newValue,
                "reason", reason
        );
    }
}
