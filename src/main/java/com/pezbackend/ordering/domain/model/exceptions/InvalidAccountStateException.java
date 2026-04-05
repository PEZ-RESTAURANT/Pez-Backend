package com.pezbackend.ordering.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class InvalidAccountStateException extends BusinessRuleException {
    public InvalidAccountStateException(String message) {
        super(message);
    }
}