package com.pezbackend.ordering.domain.model.commands;

import java.math.BigDecimal;

public record AddItemToAccountCommand(
        Long accountId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        String note
) {
    public AddItemToAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("AccountId is required.");

        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("Product name is required.");

        if (unitPrice == null || unitPrice.doubleValue() <= 0)
            throw new IllegalArgumentException("Unit price must be greater than zero.");

        if (quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
    }
}