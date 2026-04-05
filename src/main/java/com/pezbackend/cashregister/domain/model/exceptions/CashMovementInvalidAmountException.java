package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

import java.math.BigDecimal;

public class CashMovementInvalidAmountException extends BusinessRuleException {
    public CashMovementInvalidAmountException(BigDecimal amount) {
        super("Invalid cash movement amount: " + amount);
    }
}