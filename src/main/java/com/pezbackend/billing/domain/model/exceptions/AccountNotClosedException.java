package com.pezbackend.billing.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class AccountNotClosedException extends BusinessRuleException {
    public AccountNotClosedException(Long id) {
        super("The account " + id + " hasn't been closed");
    }
}
