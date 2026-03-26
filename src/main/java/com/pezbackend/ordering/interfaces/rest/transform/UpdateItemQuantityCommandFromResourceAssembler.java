package com.pezbackend.ordering.interfaces.rest.transform;

import com.pezbackend.ordering.domain.model.commands.UpdateItemQuantityCommand;
import com.pezbackend.ordering.interfaces.rest.resources.UpdateItemQuantityResource;

public class UpdateItemQuantityCommandFromResourceAssembler {

    public static UpdateItemQuantityCommand toCommandFromResource(
            Long accountId,
            UpdateItemQuantityResource resource
    ) {
        return new UpdateItemQuantityCommand(
                accountId,
                resource.itemId(),
                resource.quantity()
        );
    }
}