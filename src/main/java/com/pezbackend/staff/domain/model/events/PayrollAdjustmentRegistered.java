package com.pezbackend.staff.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento de dominio emitido cuando se registra un ajuste de nómina.
 */
public record PayrollAdjustmentRegistered(
        Long staffProfileId,
        Long adjustmentId,
        String type,
        BigDecimal amount,
        String registeredBy,
        LocalDateTime timestamp
) implements DomainEvent {

    public PayrollAdjustmentRegistered(Long staffProfileId, Long adjustmentId, String type, BigDecimal amount, String registeredBy) {
        this(staffProfileId, adjustmentId, type, amount, registeredBy, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "PayrollAdjustmentRegistered";
    }

    @Override
    public String module() {
        return "staff";
    }

    @Override
    public String userId() {
        return registeredBy;
    }

    @Override
    public Object payload() {
        return Map.of(
                "staffProfileId", staffProfileId,
                "adjustmentId", adjustmentId,
                "type", type,
                "amount", amount
        );
    }
}
