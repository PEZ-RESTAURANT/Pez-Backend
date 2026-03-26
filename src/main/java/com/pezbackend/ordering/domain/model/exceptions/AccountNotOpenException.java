package com.pezbackend.ordering.domain.model.exceptions;

public class AccountNotOpenException extends RuntimeException {
    public AccountNotOpenException() {
        super("Account is not open for modifications.");
    }
}