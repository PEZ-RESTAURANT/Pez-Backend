package com.pezbackend.cashregister.domain.model.commands;

import java.math.BigDecimal;

public record AddSaleIncomeCommand(
        BigDecimal amount,
        String note
) {}