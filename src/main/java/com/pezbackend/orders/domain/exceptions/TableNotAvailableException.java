package com.pezbackend.orders.domain.exceptions;

import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;

/**
 * Excepción lanzada cuando una mesa no está disponible para una operación (ej. está ocupada o fusionada).
 */
public class TableNotAvailableException extends BusinessRuleViolationException {
    
    public TableNotAvailableException(String message) {
        super("TABLE_NOT_AVAILABLE", message);
    }
}
