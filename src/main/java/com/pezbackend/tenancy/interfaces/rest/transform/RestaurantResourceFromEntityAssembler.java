package com.pezbackend.tenancy.interfaces.rest.transform;

import com.pezbackend.tenancy.domain.model.aggregates.Restaurant;
import com.pezbackend.tenancy.interfaces.rest.resources.RestaurantResource;

/**
 * Assembler to transform {@link Restaurant} into {@link RestaurantResource}.
 */
public class RestaurantResourceFromEntityAssembler {
    public static RestaurantResource toResourceFromEntity(Restaurant entity) {
        return new RestaurantResource(
                entity.getId(),
                entity.getName(),
                entity.getBusinessDocumentNumber(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getActive(),
                entity.getCreatedAt()
        );
    }
}
