package com.pezbackend.cashregister.domain.model.commands;

import java.math.BigDecimal;

public record AddSaleIncomeCommand(
        Long cashRegisterId,
        BigDecimal amount,
        String accountName,
        Long accountId
) {}