package com.pezbackend.cashregister.domain.model.valueobjects;

import java.math.BigDecimal;

public record MovementsSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        int countExpense,
        int countIncome,
        BigDecimal balance
) {}