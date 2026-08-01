package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

/**
 * Recurso DTO para actualizar la ubicación visual de una mesa.
 */
public record UpdateTablePositionResource(
        @NotNull(message = "La coordenada X es obligatoria.")
        Integer positionX,

        @NotNull(message = "La coordenada Y es obligatoria.")
        Integer positionY
) {}
