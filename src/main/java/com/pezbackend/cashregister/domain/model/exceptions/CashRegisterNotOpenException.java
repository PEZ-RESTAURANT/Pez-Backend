package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class CashRegisterNotOpenException extends BusinessRuleException {
    public CashRegisterNotOpenException() {
        super("The cash register is not open");
    }
}