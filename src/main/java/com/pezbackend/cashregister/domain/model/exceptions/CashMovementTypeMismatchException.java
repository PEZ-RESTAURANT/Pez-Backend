package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;

public class CashMovementTypeMismatchException extends RuntimeException {
    public CashMovementTypeMismatchException(CashMovementType expected, CashMovementType actual) {
        super("Expected movement type " + expected + " but got " + actual);
    }
}