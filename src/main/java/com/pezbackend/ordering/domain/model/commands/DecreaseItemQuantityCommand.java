package com.pezbackend.ordering.domain.model.commands;

public record DecreaseItemQuantityCommand(
        Long accountId,
        Long itemId
) {}