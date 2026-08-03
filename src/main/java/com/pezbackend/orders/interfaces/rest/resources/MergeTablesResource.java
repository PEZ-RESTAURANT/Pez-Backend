package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Recurso DTO para fusionar mesas.
 */
public record MergeTablesResource(
        @NotEmpty(message = "Debe especificar al menos una mesa para fusionar.")
        List<Long> tableIds
) {}
