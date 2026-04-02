package com.pezbackend.billing.domain.model.exceptions;

public class SaleAlreadyClosedException extends RuntimeException {
    public SaleAlreadyClosedException() {
        super("The sale is already closed");
    }
}