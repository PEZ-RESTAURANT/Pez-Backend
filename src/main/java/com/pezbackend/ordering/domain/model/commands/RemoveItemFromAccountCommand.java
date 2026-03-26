package com.pezbackend.ordering.domain.model.commands;

public record RemoveItemFromAccountCommand(
        Long accountId,
        Long itemId
) {
    public RemoveItemFromAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("AccountId is required.");

        if (itemId == null || itemId <= 0)
            throw new IllegalArgumentException("ItemId is required.");
    }
}