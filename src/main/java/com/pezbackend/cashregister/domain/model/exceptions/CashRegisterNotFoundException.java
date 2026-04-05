package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.NotFoundException;

public class CashRegisterNotFoundException extends NotFoundException {
    public CashRegisterNotFoundException(Long id) {
        super("Cash register not found with id: " + id);
    }
}