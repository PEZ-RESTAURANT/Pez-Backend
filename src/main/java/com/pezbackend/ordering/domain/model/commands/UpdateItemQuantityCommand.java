package com.pezbackend.ordering.domain.model.commands;

public record UpdateItemQuantityCommand(
        Long accountId,
        Long itemId,
        Integer quantity
) {
    public UpdateItemQuantityCommand {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("AccountId is required.");

        if (itemId == null || itemId <= 0)
            throw new IllegalArgumentException("ItemId is required.");

        if (quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
    }
}