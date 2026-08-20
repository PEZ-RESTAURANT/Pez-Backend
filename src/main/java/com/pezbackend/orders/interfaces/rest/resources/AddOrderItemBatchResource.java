package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO para la adición de múltiples platos a una comanda en lote.
 */
public record AddOrderItemBatchResource(
        @NotEmpty(message = "La lista de platos no puede estar vacía.")
        @Valid
        List<AddOrderItemResource> items
) {}
