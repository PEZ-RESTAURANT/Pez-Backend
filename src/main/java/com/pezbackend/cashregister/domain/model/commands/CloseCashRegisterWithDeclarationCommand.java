package com.pezbackend.cashregister.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;
import java.math.BigDecimal;

/**
 * Comando para cerrar caja declarando un monto en efectivo.
 *
 * @param cashRegisterId ID de la caja registradora
 * @param declaredAmount monto declarado por el cajero
 */
public record CloseCashRegisterWithDeclarationCommand(
        Long cashRegisterId,
        BigDecimal declaredAmount
) {
    public CloseCashRegisterWithDeclarationCommand {
        if (cashRegisterId == null || cashRegisterId <= 0) {
            throw new BadRequestException("CashRegisterId is required");
        }
        if (declaredAmount == null || declaredAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Declared amount must be zero or positive");
        }
    }
}
