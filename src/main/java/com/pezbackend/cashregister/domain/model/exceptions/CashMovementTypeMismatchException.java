package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class CashMovementTypeMismatchException extends BusinessRuleException {
    public CashMovementTypeMismatchException(CashMovementType expected, CashMovementType actual) {
        super("Expected movement type " + expected + " but got " + actual);
    }
}