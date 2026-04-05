package com.pezbackend.ordering.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class EmptyAccountException extends BusinessRuleException {
    public EmptyAccountException() {
        super("Cannot close an empty account");
    }
}