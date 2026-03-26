package com.pezbackend.billing.domain.model.exceptions;

public class AccountNotClosedException extends RuntimeException {
    public AccountNotClosedException(Long id) {
        super("The account "+ id +" hasn't been closed");
    }
}
