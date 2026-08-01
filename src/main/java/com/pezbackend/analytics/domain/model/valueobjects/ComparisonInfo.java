package com.pezbackend.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Record que representa la comparación entre dos períodos A y B.
 */
public record ComparisonInfo(
        BigDecimal revenueA,
        BigDecimal revenueB,
        BigDecimal revenueDiff,
        BigDecimal expensesA,
        BigDecimal expensesB,
        BigDecimal expensesDiff,
        BigDecimal netProfitA,
        BigDecimal netProfitB,
        BigDecimal netProfitDiff,
        long salesCountA,
        long salesCountB,
        long salesCountDiff,
        BigDecimal ticketAverageA,
        BigDecimal ticketAverageB,
        BigDecimal ticketAverageDiff
) {}
