package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.NotFoundException;

public class CashMovementNotFoundException extends NotFoundException {
    public CashMovementNotFoundException(Long id) {
        super("Cash movement not found with id: " + id);
    }
}