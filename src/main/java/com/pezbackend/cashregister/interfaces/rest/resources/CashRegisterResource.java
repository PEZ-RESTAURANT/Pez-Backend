package com.pezbackend.cashregister.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

public record CashRegisterResource(
        Long id,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        String status,
        List<CashMovementResource> movements
) {}