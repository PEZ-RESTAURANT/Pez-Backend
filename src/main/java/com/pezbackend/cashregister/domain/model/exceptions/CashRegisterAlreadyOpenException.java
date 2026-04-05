package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.ConflictException;

public class CashRegisterAlreadyOpenException extends ConflictException {
    public CashRegisterAlreadyOpenException() {
        super("A cash register is already open");
    }
}