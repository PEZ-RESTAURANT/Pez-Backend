package com.pezbackend.cashregister.domain.model.exceptions;

public class InvalidCashRegisterOperationException extends RuntimeException {
    public InvalidCashRegisterOperationException(String message) {
        super(message);
    }
}