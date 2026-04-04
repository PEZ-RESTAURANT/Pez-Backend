package com.pezbackend.ordering.domain.model.commands;

public record AddProductToAccountCommand(
        Long accountId,
        Long productId
) {}