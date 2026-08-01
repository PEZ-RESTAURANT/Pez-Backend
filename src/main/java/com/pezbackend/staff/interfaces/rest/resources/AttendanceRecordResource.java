package com.pezbackend.staff.interfaces.rest.resources;

/**
 * Recurso DTO para representar el registro de asistencia.
 */
public record AttendanceRecordResource(
        Long id,
        Long staffProfileId,
        String checkInAt,
        String checkOutAt,
        String method
) {}
