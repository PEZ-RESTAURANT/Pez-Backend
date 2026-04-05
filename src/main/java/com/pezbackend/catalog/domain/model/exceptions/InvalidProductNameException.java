package com.pezbackend.catalog.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public class InvalidProductNameException extends BadRequestException {
    public InvalidProductNameException() {
        super("Product name cannot be empty");
    }
}