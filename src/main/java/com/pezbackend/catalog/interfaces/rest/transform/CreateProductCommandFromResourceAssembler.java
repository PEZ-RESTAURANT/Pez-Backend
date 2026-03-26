package com.pezbackend.catalog.interfaces.rest.transform;

import com.pezbackend.catalog.domain.model.commands.CreateProductCommand;
import com.pezbackend.catalog.interfaces.rest.resources.CreateProductResource;

public class CreateProductCommandFromResourceAssembler {

    public static CreateProductCommand toCommandFromResource(CreateProductResource resource) {
        return new CreateProductCommand(
                resource.name(),
                resource.price(),
                resource.category()
        );
    }
}