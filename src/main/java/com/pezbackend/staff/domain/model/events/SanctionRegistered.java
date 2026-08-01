package com.pezbackend.staff.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento de dominio emitido cuando se aplica una sanción.
 */
public record SanctionRegistered(
        Long staffProfileId,
        Long sanctionId,
        String type,
        String registeredBy,
        LocalDateTime timestamp
) implements DomainEvent {

    public SanctionRegistered(Long staffProfileId, Long sanctionId, String type, String registeredBy) {
        this(staffProfileId, sanctionId, type, registeredBy, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "SanctionRegistered";
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
                "sanctionId", sanctionId,
                "type", type
        );
    }
}
