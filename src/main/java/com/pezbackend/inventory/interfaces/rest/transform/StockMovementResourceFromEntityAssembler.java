package com.pezbackend.inventory.interfaces.rest.transform;

import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.interfaces.rest.resources.StockMovementResource;

/**
 * Ensamblador para transformar la entidad StockMovement en su recurso StockMovementResource DTO.
 */
public class StockMovementResourceFromEntityAssembler {

    /**
     * Convierte una entidad StockMovement en un DTO.
     *
     * @param entity la entidad StockMovement
     * @return el DTO correspondiente
     */
    public static StockMovementResource toResourceFromEntity(StockMovement entity) {
        return new StockMovementResource(
                entity.getId(),
                entity.getSupplyId(),
                entity.getType(),
                entity.getQuantity(),
                entity.getRegisteredBy(),
                entity.getDate(),
                entity.getReason()
        );
    }
}
