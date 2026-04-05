package com.pezbackend.catalog.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.ConflictException;

public class ProductAlreadyExistsException extends ConflictException {
    public ProductAlreadyExistsException(String name) {
        super("Product already exists with name: " + name);
    }
}
