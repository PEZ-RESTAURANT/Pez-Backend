package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTableResource(
        @NotNull(message = "El número de mesa es obligatorio.")
        @Positive(message = "El número de mesa debe ser positivo.")
        Integer number,

        @NotNull(message = "El piso es obligatorio.")
        Integer floor,

        String zoneTag
) {}
