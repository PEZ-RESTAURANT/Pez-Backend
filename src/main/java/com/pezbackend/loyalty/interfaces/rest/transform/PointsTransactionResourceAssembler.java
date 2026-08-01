package com.pezbackend.loyalty.interfaces.rest.transform;

import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.interfaces.rest.resources.PointsTransactionResource;

/**
 * Ensamblador para convertir la entidad PointsTransaction a su DTO PointsTransactionResource.
 */
public class PointsTransactionResourceAssembler {

    public static PointsTransactionResource toResource(PointsTransaction transaction) {
        return new PointsTransactionResource(
                transaction.getId(),
                transaction.getCustomerId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getSaleId(),
                transaction.getDate().toString()
        );
    }
}
