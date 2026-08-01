package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Recurso DTO para crear una nueva mesa.
 */
public record CreateTableResource(
        @NotNull(message = "El número de mesa es obligatorio.")
        @Positive(message = "El número de mesa debe ser positivo.")
        Integer number,

        @NotNull(message = "El piso es obligatorio.")
        Integer floor,

        String zoneTag,

        @NotNull(message = "La coordenada X es obligatoria.")
        Integer positionX,

        @NotNull(message = "La coordenada Y es obligatoria.")
        Integer positionY
) {}
