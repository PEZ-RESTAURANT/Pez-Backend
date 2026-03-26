package com.pezbackend.ordering.domain.model.exceptions;

public class EmptyAccountException extends RuntimeException {
    public EmptyAccountException() {
        super("Cannot close an empty account");
    }
}