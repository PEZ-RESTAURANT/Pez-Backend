package com.pezbackend.cashregister.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.math.BigDecimal;

public record OpenCashRegisterCommand(BigDecimal openingBalance) {
    public OpenCashRegisterCommand {
        if (openingBalance == null || openingBalance.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Opening balance must be greater than zero");
    }
}