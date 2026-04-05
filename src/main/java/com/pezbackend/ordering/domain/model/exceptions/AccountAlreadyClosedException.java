package com.pezbackend.ordering.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class AccountAlreadyClosedException extends BusinessRuleException {
    public AccountAlreadyClosedException() {
        super("Account is not modifiable, is closed");
    }
}