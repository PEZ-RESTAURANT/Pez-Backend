package com.pezbackend.cashregister.interfaces.rest.resources;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashMovementResource(
        Long id,
        CashMovementType type,
        BigDecimal amount,
        String note,
        LocalDateTime createdAt
) {}