package com.pezbackend.staff.interfaces.rest.resources;

/**
 * Recurso DTO para resolver una asistencia sin salida.
 */
public record ResolveAttendanceResource(
        String checkOutAt
) {}
