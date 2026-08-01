package com.pezbackend.orders.interfaces.rest.resources;

import com.pezbackend.orders.domain.model.valueobjects.TableStatus;

/**
 * Recurso DTO para exponer la información de una mesa del local.
 */
public record RestaurantTableResource(
        Long id,
        Integer number,
        Integer floor,
        String zoneTag,
        Integer positionX,
        Integer positionY,
        TableStatus status
) {}
