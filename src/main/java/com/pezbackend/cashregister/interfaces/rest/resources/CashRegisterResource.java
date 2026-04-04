package com.pezbackend.cashregister.interfaces.rest.resources;

import com.pezbackend.cashregister.domain.model.valueobjects.MovementsSummary;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record CashRegisterResource(
        Long id,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        String status,
        List<CashMovementResource> movements,
        MovementsSummary summary,
        Date createdAt
) {}