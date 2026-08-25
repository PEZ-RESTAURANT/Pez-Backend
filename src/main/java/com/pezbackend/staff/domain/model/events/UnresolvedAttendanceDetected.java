package com.pezbackend.staff.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Evento emitido al detectar un registro de asistencia sin salida al cerrar turno.
 */
public record UnresolvedAttendanceDetected(
        Long attendanceRecordId,
        Long staffProfileId,
        LocalDateTime checkInAt,
        Long restaurantId,
        List<String> notifiedRoles,
        LocalDateTime timestamp
) implements DomainEvent {

    public UnresolvedAttendanceDetected(Long attendanceRecordId, Long staffProfileId, LocalDateTime checkInAt, Long restaurantId, List<String> notifiedRoles) {
        this(attendanceRecordId, staffProfileId, checkInAt, restaurantId, notifiedRoles, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "UnresolvedAttendanceDetected";
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
                "attendanceRecordId", attendanceRecordId,
                "staffProfileId", staffProfileId,
                "checkInAt", checkInAt.toString(),
                "restaurantId", restaurantId,
                "notifiedRoles", notifiedRoles
        );
    }
}
