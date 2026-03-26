package com.pezbackend.ordering.interfaces.rest.transform;

import com.pezbackend.ordering.domain.model.commands.AddItemToAccountCommand;
import com.pezbackend.ordering.interfaces.rest.resources.AddItemToAccountResource;

public class AddItemToAccountCommandFromResourceAssembler {

    public static AddItemToAccountCommand toCommandFromResource(
            Long accountId,
            AddItemToAccountResource resource
    ) {
        return new AddItemToAccountCommand(
                accountId,
                resource.productName(),
                resource.unitPrice(),
                resource.quantity(),
                resource.note()
        );
    }
}