package com.pezbackend.cashregister.domain.model.commands;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;

import java.math.BigDecimal;

public record AddCashMovementCommand(
        Long cashRegisterId,
        CashMovementType type,
        BigDecimal amount,
        String note
) {}