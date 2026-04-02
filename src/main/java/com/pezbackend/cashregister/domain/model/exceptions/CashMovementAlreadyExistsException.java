package com.pezbackend.cashregister.domain.model.exceptions;

public class CashMovementAlreadyExistsException extends RuntimeException {
    public CashMovementAlreadyExistsException(Long movementId) {
        super("Cash movement already exists with id: " + movementId);
    }
}