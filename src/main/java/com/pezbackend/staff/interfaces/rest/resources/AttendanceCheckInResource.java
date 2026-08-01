package com.pezbackend.staff.interfaces.rest.resources;

/**
 * Recurso DTO para registrar entrada de asistencia.
 */
public record AttendanceCheckInResource(
        Long staffProfileId,
        String method,
        String checkInAt
) {}
