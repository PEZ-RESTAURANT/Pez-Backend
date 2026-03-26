package com.pezbackend.ordering.domain.model.commands;

public record NameAccountCommand(
        Long accountId,
        String name
) {
    public NameAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("AccountId is required.");

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Account name cannot be empty.");
    }
}