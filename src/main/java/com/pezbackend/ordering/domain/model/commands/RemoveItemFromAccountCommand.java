package com.pezbackend.ordering.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record RemoveItemFromAccountCommand(
        Long accountId,
        Long itemId
) {
    public RemoveItemFromAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new BadRequestException("AccountId is required.");

        if (itemId == null || itemId <= 0)
            throw new BadRequestException("ItemId is required.");
    }
}