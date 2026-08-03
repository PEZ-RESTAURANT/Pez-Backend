package com.pezbackend.orders.interfaces.rest.resources;

import java.time.LocalDateTime;

/**
 * Representación DTO de respuesta para la reserva.
 */
public record ReservationResource(
        Long id,
        String customerName,
        String customerPhone,
        Long customerId,
        LocalDateTime reservationDateTime,
        Integer partySize,
        String notes,
        Long tableId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
