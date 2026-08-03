package com.pezbackend.orders.domain.exceptions;

import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;

/**
 * Excepción lanzada cuando se intenta fusionar una mesa que ya pertenece a otra fusión.
 */
public class TableAlreadyMergedException extends BusinessRuleViolationException {
    
    public TableAlreadyMergedException(String message) {
        super("TABLE_ALREADY_MERGED", message);
    }
}
