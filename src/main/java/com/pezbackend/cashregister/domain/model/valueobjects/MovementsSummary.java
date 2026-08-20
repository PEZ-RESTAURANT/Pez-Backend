package com.pezbackend.cashregister.domain.model.valueobjects;

import java.math.BigDecimal;

public record MovementsSummary(
        BigDecimal totalSales,
        BigDecimal totalManualIncome,
        BigDecimal totalManualExpense,
        int countExpense,
        int countIncome,
        BigDecimal balance
) {}