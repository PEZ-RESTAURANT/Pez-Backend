package com.pezbackend.cashregister.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class CashRegisterHasNoMovementsException extends BusinessRuleException {
    public CashRegisterHasNoMovementsException(Long id) {
        super("Cash register with id " + id + " has no movements");
    }
}