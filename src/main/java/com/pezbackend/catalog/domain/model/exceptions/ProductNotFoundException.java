package com.pezbackend.catalog.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
