package com.pezbackend.cashregister.domain.model.exceptions;

import java.math.BigDecimal;

public class CashMovementInvalidAmountException extends RuntimeException {
    public CashMovementInvalidAmountException(BigDecimal amount) {
        super("Invalid cash movement amount: " + amount);
    }
}