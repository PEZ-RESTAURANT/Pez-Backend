package com.pezbackend.cashregister.interfaces.rest.resources;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la representación del recurso CashMovement.
 */
public record CashMovementResource(
        Long id,
        CashMovementType type,
        BigDecimal amount,
        String reason,
        String note,
        LocalDateTime createdAt
) {}