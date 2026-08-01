package com.pezbackend.cashregister.domain.model.commands;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementReason;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.math.BigDecimal;

/**
 * Comando para registrar un movimiento manual de caja.
 */
public record AddCashMovementCommand(
        CashMovementType type,
        BigDecimal amount,
        CashMovementReason reason,
        String note
) {
    public AddCashMovementCommand {
        if (type == null)
            throw new BadRequestException("Movement type is required");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Amount must be greater than zero");

        if (reason == null)
            throw new BadRequestException("Reason is required");

        if (reason == CashMovementReason.OTHER && (note == null || note.isBlank())) {
            throw new BusinessRuleViolationException("NOTE_REQUIRED_FOR_OTHER", "Texto libre obligatorio para el motivo OTHER.");
        }
    }

    // Constructor de compatibilidad para evitar romper otros componentes si es necesario
    public AddCashMovementCommand(CashMovementType type, BigDecimal amount, String note) {
        this(type, amount, CashMovementReason.OTHER, note);
    }
}