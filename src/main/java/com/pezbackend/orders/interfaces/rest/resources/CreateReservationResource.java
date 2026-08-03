package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * Recurso DTO para crear una reserva.
 */
public record CreateReservationResource(
        @NotBlank(message = "El nombre del cliente es obligatorio.")
        String customerName,

        @NotBlank(message = "El teléfono del cliente es obligatorio.")
        String customerPhone,

        Long customerId,

        @NotNull(message = "La fecha y hora de la reserva es obligatoria.")
        LocalDateTime reservationDateTime,

        @NotNull(message = "El tamaño del grupo es obligatorio.")
        @Positive(message = "El tamaño del grupo debe ser mayor a cero.")
        Integer partySize,

        String notes,

        Long tableId
) {}
