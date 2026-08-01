package com.pezbackend.cashregister.interfaces.rest.resources;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementReason;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import java.math.BigDecimal;

/**
 * Recurso DTO para recibir el request de adición de movimiento de caja manual.
 */
public record AddCashMovementResource(
        CashMovementType type,
        BigDecimal amount,
        CashMovementReason reason,
        String note
) {}