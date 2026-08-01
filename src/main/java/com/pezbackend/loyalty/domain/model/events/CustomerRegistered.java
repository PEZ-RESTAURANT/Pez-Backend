package com.pezbackend.loyalty.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al registrar a un cliente afiliado.
 */
public record CustomerRegistered(
        Long customerId,
        String phone,
        String fullName,
        LocalDateTime timestamp
) implements DomainEvent {

    public CustomerRegistered(Long customerId, String phone, String fullName) {
        this(customerId, phone, fullName, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "CustomerRegistered";
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
                "phone", phone,
                "fullName", fullName
        );
    }
}
