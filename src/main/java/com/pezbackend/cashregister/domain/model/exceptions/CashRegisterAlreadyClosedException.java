package com.pezbackend.cashregister.domain.model.exceptions;

public class CashRegisterAlreadyClosedException extends RuntimeException {
    public CashRegisterAlreadyClosedException() {
        super("The cash register is already closed");
    }
}