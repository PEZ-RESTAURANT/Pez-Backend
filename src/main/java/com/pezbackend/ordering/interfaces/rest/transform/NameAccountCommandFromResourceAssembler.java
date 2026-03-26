package com.pezbackend.ordering.interfaces.rest.transform;

import com.pezbackend.ordering.domain.model.commands.NameAccountCommand;
import com.pezbackend.ordering.interfaces.rest.resources.NameAccountResource;

public class NameAccountCommandFromResourceAssembler {

    public static NameAccountCommand toCommandFromResource(
            Long accountId,
            NameAccountResource resource
    ) {
        return new NameAccountCommand(
                accountId,
                resource.name()
        );
    }
}