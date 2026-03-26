package com.pezbackend.ordering.domain.model.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long accountId) {
        super("Account with id " + accountId + " not found.");
    }
}