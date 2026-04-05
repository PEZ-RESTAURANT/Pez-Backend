package com.pezbackend.ordering.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record NameAccountCommand(
        Long accountId,
        String name
) {
    public NameAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new BadRequestException("AccountId is required.");

        if (name == null || name.isBlank())
            throw new BadRequestException("Account name cannot be empty.");
    }
}