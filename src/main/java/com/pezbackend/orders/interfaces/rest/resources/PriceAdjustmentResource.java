package com.pezbackend.orders.interfaces.rest.resources;

import com.pezbackend.orders.domain.model.valueobjects.PriceAdjustmentScope;
import com.pezbackend.orders.domain.model.valueobjects.PriceAdjustmentValidity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recurso DTO para exponer la información de un ajuste de precio aplicado.
 */
public record PriceAdjustmentResource(
        Long id,
        PriceAdjustmentScope scope,
        PriceAdjustmentValidity validity,
        LocalDateTime startAt,
        LocalDateTime endAt,
        BigDecimal newValue,
        String appliedBy,
        String reason
) {}
