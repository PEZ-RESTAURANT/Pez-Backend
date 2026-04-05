package com.pezbackend.ordering.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.NotFoundException;

public class AccountItemNotFoundException extends NotFoundException {
    public AccountItemNotFoundException(Long itemId) {
        super("Account item with id " + itemId + " not found.");
    }
}