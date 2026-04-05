package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class CashRegisterAlreadyClosedException extends BusinessRuleException {
    public CashRegisterAlreadyClosedException() {
        super("The cash register is already closed");
    }
}