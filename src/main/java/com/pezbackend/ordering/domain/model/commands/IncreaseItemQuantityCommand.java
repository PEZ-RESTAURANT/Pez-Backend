package com.pezbackend.ordering.domain.model.commands;

public record IncreaseItemQuantityCommand(
        Long accountId,
        Long itemId
) {}
