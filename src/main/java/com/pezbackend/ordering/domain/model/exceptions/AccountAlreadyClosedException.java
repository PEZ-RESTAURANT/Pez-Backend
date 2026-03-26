package com.pezbackend.ordering.domain.model.exceptions;

public class AccountAlreadyClosedException extends RuntimeException {
    public AccountAlreadyClosedException() {
        super("Account is already closed");
    }
}