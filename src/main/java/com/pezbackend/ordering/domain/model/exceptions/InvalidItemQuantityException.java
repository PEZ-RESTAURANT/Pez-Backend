package com.pezbackend.ordering.domain.model.exceptions;

public class InvalidItemQuantityException extends RuntimeException {
    public InvalidItemQuantityException(Integer quantity) {
        super("Invalid quantity: " + quantity);
    }
}