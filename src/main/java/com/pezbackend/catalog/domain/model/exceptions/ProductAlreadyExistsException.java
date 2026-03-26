package com.pezbackend.catalog.domain.model.exceptions;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String name) {
        super("Product already with name: " + name);
    }
}
