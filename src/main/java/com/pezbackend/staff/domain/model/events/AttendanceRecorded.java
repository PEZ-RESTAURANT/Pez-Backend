package com.pezbackend.staff.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al registrar asistencia.
 */
public record AttendanceRecorded(
        Long staffProfileId,
        Long recordId,
        String method,
        boolean checkIn,
        LocalDateTime timestamp
) implements DomainEvent {

    public AttendanceRecorded(Long staffProfileId, Long recordId, String method, boolean checkIn) {
        this(staffProfileId, recordId, method, checkIn, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "AttendanceRecorded";
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
                "staffProfileId", staffProfileId,
                "recordId", recordId,
                "method", method,
                "checkIn", checkIn
        );
    }
}
