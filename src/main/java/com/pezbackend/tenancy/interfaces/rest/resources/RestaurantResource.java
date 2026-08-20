package com.pezbackend.tenancy.interfaces.rest.resources;

import java.time.LocalDateTime;

/**
 * Recurso REST que representa un restaurante.
 */
public record RestaurantResource(
        Long id,
        String name,
        String businessDocumentNumber,
        String contactEmail,
        String contactPhone,
        String address,
        Boolean active,
        LocalDateTime createdAt
) {
}
