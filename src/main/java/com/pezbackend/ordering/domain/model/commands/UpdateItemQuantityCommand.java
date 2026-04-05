package com.pezbackend.ordering.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record UpdateItemQuantityCommand(
        Long accountId,
        Long itemId,
        Integer quantity
) {
    public UpdateItemQuantityCommand {
        if (accountId == null || accountId <= 0)
            throw new BadRequestException("AccountId is required.");

        if (itemId == null || itemId <= 0)
            throw new BadRequestException("ItemId is required.");

        if (quantity == null || quantity <= 0)
            throw new BadRequestException("Quantity must be greater than zero.");
    }
}