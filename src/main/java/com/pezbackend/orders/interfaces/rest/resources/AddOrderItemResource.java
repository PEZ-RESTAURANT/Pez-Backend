package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Recurso DTO para añadir un plato a la comanda.
 */
public record AddOrderItemResource(
        @NotNull(message = "El ID de producto es obligatorio.")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad debe ser como mínimo 1.")
        Integer quantity,

        String note,

        @NotNull(message = "El ID de mozo que comanda es obligatorio.")
        Long waiterId
) {}
