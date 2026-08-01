package com.pezbackend.inventory.interfaces.rest.transform;

import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.interfaces.rest.resources.SupplyResource;

/**
 * Ensamblador para transformar la entidad Supply en su recurso SupplyResource DTO.
 */
public class SupplyResourceFromEntityAssembler {

    /**
     * Convierte una entidad Supply en un DTO.
     *
     * @param entity la entidad Supply
     * @return el DTO correspondiente
     */
    public static SupplyResource toResourceFromEntity(Supply entity) {
        return new SupplyResource(
                entity.getId(),
                entity.getName(),
                entity.getUnit(),
                entity.getCurrentStock(),
                entity.getMinThreshold()
        );
    }
}
