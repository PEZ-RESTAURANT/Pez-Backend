package com.pezbackend.orders.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recurso DTO para aplicar un ajuste manual de precio sobre la comanda.
 */
public record ApplyPriceAdjustmentResource(
        @NotBlank(message = "El alcance (INDIVIDUAL, GROUP, ALL) es obligatorio.")
        String scope,

        @NotBlank(message = "El tipo de validez (PERMANENT, TEMPORARY) es obligatorio.")
        String validity,

        LocalDateTime startAt,

        LocalDateTime endAt,

        @NotNull(message = "El nuevo valor ajustado es obligatorio.")
        BigDecimal newValue,

        @NotBlank(message = "El motivo o justificación del ajuste es obligatorio.")
        String reason
) {}
