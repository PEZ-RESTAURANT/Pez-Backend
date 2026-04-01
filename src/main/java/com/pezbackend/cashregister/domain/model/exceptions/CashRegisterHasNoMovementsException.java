package com.pezbackend.cashregister.domain.model.exceptions;

public class CashRegisterHasNoMovementsException extends RuntimeException {
    public CashRegisterHasNoMovementsException(Long cashRegisterId) {
        super("Cash register with id " + cashRegisterId + " has no movements");
    }
}