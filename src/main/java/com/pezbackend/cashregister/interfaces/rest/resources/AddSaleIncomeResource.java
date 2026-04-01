package com.pezbackend.cashregister.interfaces.rest.resources;

import java.math.BigDecimal;

public record AddSaleIncomeResource(
        BigDecimal amount,
        String accountName,
        Long accountId
) {}