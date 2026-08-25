package com.pezbackend.staff.interfaces.rest.resources;

/**
 * Representa el recurso DTO recibido para un evento de marcado de huella dactilar.
 */
public record FingerprintAttendanceEventResource(
        String deviceSerialNumber,
        Integer deviceUserId,
        String timestamp
) {}
