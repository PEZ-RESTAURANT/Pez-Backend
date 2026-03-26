package com.pezbackend.ordering.domain.model.exceptions;

public class InvalidItemQuantityException extends RuntimeException {
    public InvalidItemQuantityException(Integer quantity) {
        super("Invalid item quantity: " + quantity + ". Quantity must be greater than 0.");
    }
}