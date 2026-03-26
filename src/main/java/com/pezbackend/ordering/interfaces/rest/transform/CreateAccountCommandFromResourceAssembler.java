package com.pezbackend.ordering.interfaces.rest.transform;

import com.pezbackend.ordering.domain.model.commands.CreateAccountCommand;
import com.pezbackend.ordering.interfaces.rest.resources.CreateAccountResource;

public class CreateAccountCommandFromResourceAssembler {

    public static CreateAccountCommand toCommandFromResource(CreateAccountResource resource) {
        return new CreateAccountCommand(
                resource.staffId()
        );
    }
}