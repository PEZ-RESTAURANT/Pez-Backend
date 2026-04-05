package com.pezbackend.ordering.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record CloseAccountCommand(
        Long accountId
) {
    public CloseAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new BadRequestException("AccountId is required.");
    }
}