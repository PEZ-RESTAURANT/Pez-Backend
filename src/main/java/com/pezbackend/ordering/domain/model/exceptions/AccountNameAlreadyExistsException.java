package com.pezbackend.ordering.domain.model.exceptions;

public class AccountNameAlreadyExistsException extends RuntimeException {
    public AccountNameAlreadyExistsException(String name) {
        super("An active account already exists with name: " + name);
    }
}