package com.pezbackend.staff.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento de dominio emitido cuando se registran horas extras.
 */
public record OvertimeRegistered(
        Long staffProfileId,
        Long overtimeId,
        BigDecimal hours,
        String registeredBy,
        LocalDateTime timestamp
) implements DomainEvent {

    public OvertimeRegistered(Long staffProfileId, Long overtimeId, BigDecimal hours, String registeredBy) {
        this(staffProfileId, overtimeId, hours, registeredBy, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "OvertimeRegistered";
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
                "overtimeId", overtimeId,
                "hours", hours
        );
    }
}
