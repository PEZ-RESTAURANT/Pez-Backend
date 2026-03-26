package com.pezbackend.billing.domain.model.exceptions;

public class EmptySaleException extends RuntimeException {
    public EmptySaleException() {
        super("Cannot close a sale without items");
    }
}