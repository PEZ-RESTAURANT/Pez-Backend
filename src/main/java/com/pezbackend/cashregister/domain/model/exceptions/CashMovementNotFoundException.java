package com.pezbackend.cashregister.domain.model.exceptions;

public class CashMovementNotFoundException extends RuntimeException {
    public CashMovementNotFoundException(Long id) {
        super("Cash movement not found with id: " + id);
    }
}