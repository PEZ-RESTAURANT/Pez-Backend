package com.pezbackend.staff.interfaces.rest.resources;

/**
 * Recurso DTO para registrar salida de asistencia.
 */
public record AttendanceCheckOutResource(
        Long staffProfileId,
        String checkOutAt
) {}
