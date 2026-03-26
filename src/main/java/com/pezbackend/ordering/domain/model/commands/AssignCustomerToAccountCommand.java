package com.pezbackend.ordering.domain.model.commands;

public record AssignCustomerToAccountCommand(
        Long accountId,
        String customerName,
        String dni,
        String ruc
) {
    public AssignCustomerToAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("AccountId is required.");
    }
}