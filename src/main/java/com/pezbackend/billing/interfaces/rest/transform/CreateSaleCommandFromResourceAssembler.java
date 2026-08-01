package com.pezbackend.billing.interfaces.rest.transform;

import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.interfaces.rest.resources.CreateSaleResource;

/**
 * Ensamblador para transformar CreateSaleResource a CreateSaleCommand.
 */
public class CreateSaleCommandFromResourceAssembler {

    public static CreateSaleCommand toCommandFromResource(CreateSaleResource resource) {
        return new CreateSaleCommand(
                resource.orderId(),
                resource.documentType(),
                resource.customerDocumentNumber(),
                resource.customerName()
        );
    }
}