package com.pezbackend.kitchen.interfaces.rest.transform;

import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.interfaces.rest.resources.KitchenZoneResource;

/**
 * Ensamblador para transformar entidades KitchenZone a recursos KitchenZoneResource.
 */
public class KitchenZoneResourceFromEntityAssembler {

    /**
     * Convierte una entidad KitchenZone en su representación DTO.
     *
     * @param entity la entidad KitchenZone
     * @return el recurso DTO correspondiente
     */
    public static KitchenZoneResource toResourceFromEntity(KitchenZone entity) {
        return new KitchenZoneResource(entity.getId(), entity.getName(), entity.isPrintingEnabled());
    }
}
