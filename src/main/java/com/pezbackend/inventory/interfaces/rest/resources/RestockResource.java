package com.pezbackend.inventory.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para recibir la cantidad ingresada en un reabastecimiento.
 */
public record RestockResource(
        BigDecimal quantity
) {}
