package com.pezbackend.billing.domain.model.exceptions;

public class SaleNotFoundException extends RuntimeException {
    public SaleNotFoundException(Long id) {
        super("Sale not found with id: " + id);
    }
}
