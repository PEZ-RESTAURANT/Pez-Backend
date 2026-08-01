package com.pezbackend.loyalty.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al eliminar a un cliente del programa de fidelización.
 */
public record CustomerDeleted(
        Long customerId,
        LocalDateTime timestamp
) implements DomainEvent {

    public CustomerDeleted(Long customerId) {
        this(customerId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "CustomerDeleted";
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
        return Map.of("customerId", customerId);
    }
}
