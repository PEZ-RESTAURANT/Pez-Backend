package com.pezbackend.cashregister.interfaces.rest.resources;

import com.pezbackend.cashregister.domain.model.valueobjects.MovementsSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CashRegisterResource(
        Long id,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        String status,
        MovementsSummary summary,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        List<CashMovementResource> movements
) {}