package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

/**
 * Recurso DTO para trasladar una comanda.
 */
public record TransferOrderResource(
        @NotNull(message = "El ID de la mesa de destino es obligatorio.")
        Long toTableId
) {}
