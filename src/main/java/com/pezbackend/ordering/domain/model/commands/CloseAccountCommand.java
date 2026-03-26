package com.pezbackend.ordering.domain.model.commands;

public record CloseAccountCommand(
        Long accountId
) {
    public CloseAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("AccountId is required.");
    }
}