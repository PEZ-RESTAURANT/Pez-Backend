package com.pezbackend.cashregister.domain.model.commands;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.math.BigDecimal;

public record AddCashMovementCommand(
        CashMovementType type,
        BigDecimal amount,
        String note
) {
    public AddCashMovementCommand {
        if (type == null)
            throw new BadRequestException("Movement type is required");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Amount must be greater than zero");
    }
}