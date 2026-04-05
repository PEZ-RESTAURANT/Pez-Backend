package com.pezbackend.ordering.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class AccountNameAlreadyExistsException extends BusinessRuleException {
    public AccountNameAlreadyExistsException(String name) {
        super("An active account already exists with name: " + name);
    }
}