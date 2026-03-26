package com.pezbackend.catalog.domain.services;

import com.pezbackend.catalog.domain.model.commands.CreateProductCommand;
import com.pezbackend.catalog.domain.model.commands.UpdateProductCommand;
import com.pezbackend.catalog.domain.model.commands.DeleteProductCommand;

public interface ProductCommandService {

    Long handle(CreateProductCommand command);

    void handle(UpdateProductCommand command);

    void handle(DeleteProductCommand command);
}