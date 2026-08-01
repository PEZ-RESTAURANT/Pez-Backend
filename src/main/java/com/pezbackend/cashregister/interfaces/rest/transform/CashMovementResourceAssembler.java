package com.pezbackend.cashregister.interfaces.rest.transform;

import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.interfaces.rest.resources.CashMovementResource;

/**
 * Ensamblador para convertir la entidad CashMovement a su recurso DTO CashMovementResource.
 */
public class CashMovementResourceAssembler {

    public static CashMovementResource toResource(CashMovement movement) {
        return new CashMovementResource(
                movement.getId(),
                movement.getType(),
                movement.getAmount(),
                movement.getReason() != null ? movement.getReason().name() : null,
                movement.getNote(),
                movement.getCreatedAt()
        );
    }
}