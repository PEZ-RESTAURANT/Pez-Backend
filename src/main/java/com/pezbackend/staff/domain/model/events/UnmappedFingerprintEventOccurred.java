package com.pezbackend.staff.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al detectar un marcaje de huella no mapeado.
 */
public record UnmappedFingerprintEventOccurred(
        String deviceSerialNumber,
        Integer deviceUserId,
        LocalDateTime timestamp
) implements DomainEvent {

    public UnmappedFingerprintEventOccurred(String deviceSerialNumber, Integer deviceUserId) {
        this(deviceSerialNumber, deviceUserId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "UnmappedFingerprintEventOccurred";
    }

    @Override
    public String module() {
        return "staff";
    }

    @Override
    public String userId() {
        return "system";
    }

    @Override
    public Object payload() {
        return Map.of(
                "deviceSerialNumber", deviceSerialNumber,
                "deviceUserId", deviceUserId,
                "timestamp", timestamp.toString()
        );
    }
}
