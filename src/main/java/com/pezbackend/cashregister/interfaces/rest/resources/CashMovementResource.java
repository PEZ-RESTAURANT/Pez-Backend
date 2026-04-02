package com.pezbackend.cashregister.interfaces.rest.resources;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import java.math.BigDecimal;

public record CashMovementResource(
        Long id,
        CashMovementType type,
        BigDecimal amount,
        String note
) {}