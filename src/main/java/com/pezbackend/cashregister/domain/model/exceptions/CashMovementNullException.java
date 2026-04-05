package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public class CashMovementNullException extends BadRequestException {
    public CashMovementNullException() {
        super("Cash movement cannot be null");
    }
}