package com.pezbackend.cashregister.domain.model.exceptions;

public class CashRegisterNotOpenException extends RuntimeException {
    public CashRegisterNotOpenException() {
        super("The cash register is not open");
    }
}