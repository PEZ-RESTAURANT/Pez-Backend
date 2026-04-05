package com.pezbackend.ordering.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public class InvalidItemQuantityException extends BadRequestException {
    public InvalidItemQuantityException(Integer quantity) {
        super("Invalid item quantity: " + quantity + ". Must be > 0");
    }
}