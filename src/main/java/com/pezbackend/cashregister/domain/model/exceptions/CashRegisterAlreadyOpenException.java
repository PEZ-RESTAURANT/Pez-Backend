package com.pezbackend.cashregister.domain.model.exceptions;

public class CashRegisterAlreadyOpenException extends RuntimeException {
    public CashRegisterAlreadyOpenException() {
        super("A cash register is already open");
    }
}