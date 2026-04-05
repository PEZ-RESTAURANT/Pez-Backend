package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

import java.math.BigDecimal;

public class CashRegisterInvalidOpeningBalanceException extends BusinessRuleException {
    public CashRegisterInvalidOpeningBalanceException(BigDecimal amount) {
        super("Opening balance must be greater than zero: " + amount);
    }
}
