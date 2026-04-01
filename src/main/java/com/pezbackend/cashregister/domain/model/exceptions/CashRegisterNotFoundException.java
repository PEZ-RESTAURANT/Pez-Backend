package com.pezbackend.cashregister.domain.model.exceptions;

public class CashRegisterNotFoundException extends RuntimeException {
    public CashRegisterNotFoundException(Long id) {
        super("Cash register not found with id: " + id);
    }
}