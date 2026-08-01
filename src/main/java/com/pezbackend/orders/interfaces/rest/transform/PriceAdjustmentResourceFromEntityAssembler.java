package com.pezbackend.orders.interfaces.rest.transform;

import com.pezbackend.orders.domain.model.entities.PriceAdjustment;
import com.pezbackend.orders.interfaces.rest.resources.PriceAdjustmentResource;

/**
 * Mapeador de la entidad PriceAdjustment al recurso DTO.
 */
public class PriceAdjustmentResourceFromEntityAssembler {

    public static PriceAdjustmentResource toResourceFromEntity(PriceAdjustment entity) {
        if (entity == null) return null;
        return new PriceAdjustmentResource(
                entity.getId(),
                entity.getScope(),
                entity.getValidity(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getNewValue(),
                entity.getAppliedBy(),
                entity.getReason()
        );
    }
}
