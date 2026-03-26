package com.pezbackend.ordering.domain.model.exceptions;

public class AccountItemNotFoundException extends RuntimeException {
    public AccountItemNotFoundException(Long itemId) {
        super("Account item with id " + itemId + " not found.");
    }
}