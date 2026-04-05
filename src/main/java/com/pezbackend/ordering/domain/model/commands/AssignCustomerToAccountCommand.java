package com.pezbackend.ordering.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record AssignCustomerToAccountCommand(
        Long accountId,
        String customerName,
        String dni,
        String ruc
) {
    public AssignCustomerToAccountCommand {
        if (accountId == null || accountId <= 0)
            throw new BadRequestException("AccountId is required.");
    }
}