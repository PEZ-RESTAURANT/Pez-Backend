package com.pezbackend.billing.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class EmptySaleException extends BusinessRuleException {
    public EmptySaleException() {
        super("Cannot close a sale without items");
    }
}