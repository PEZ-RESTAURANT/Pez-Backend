package com.pezbackend.ordering.interfaces.rest.transform;

import com.pezbackend.ordering.domain.model.commands.AssignCustomerToAccountCommand;
import com.pezbackend.ordering.interfaces.rest.resources.AssignCustomerResource;

public class AssignCustomerCommandFromResourceAssembler {

    public static AssignCustomerToAccountCommand toCommandFromResource(
            Long accountId,
            AssignCustomerResource resource
    ) {
        return new AssignCustomerToAccountCommand(
                accountId,
                resource.customerName(),
                resource.dni(),
                resource.ruc()
        );
    }
}