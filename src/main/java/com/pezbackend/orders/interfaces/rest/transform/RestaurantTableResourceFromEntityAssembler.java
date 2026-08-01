package com.pezbackend.orders.interfaces.rest.transform;

import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.interfaces.rest.resources.RestaurantTableResource;

/**
 * Mapeador de la entidad RestaurantTable al recurso DTO.
 */
public class RestaurantTableResourceFromEntityAssembler {

    public static RestaurantTableResource toResourceFromEntity(RestaurantTable entity) {
        if (entity == null) return null;
        return new RestaurantTableResource(
                entity.getId(),
                entity.getNumber(),
                entity.getFloor(),
                entity.getZoneTag(),
                entity.getPositionX(),
                entity.getPositionY(),
                entity.getStatus()
        );
    }
}
