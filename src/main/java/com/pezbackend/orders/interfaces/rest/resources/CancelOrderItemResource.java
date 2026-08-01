package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Recurso DTO para cancelar un plato de la comanda con justificación.
 */
public record CancelOrderItemResource(
        @NotBlank(message = "El motivo de la cancelación es obligatorio.")
        String cancellationReason,

        String detail
) {}
