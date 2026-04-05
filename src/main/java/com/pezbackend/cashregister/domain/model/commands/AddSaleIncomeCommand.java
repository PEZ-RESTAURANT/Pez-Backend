package com.pezbackend.cashregister.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.math.BigDecimal;

public record AddSaleIncomeCommand(
        BigDecimal amount,
        String note
) {
    public AddSaleIncomeCommand {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Amount must be greater than zero");
    }
}