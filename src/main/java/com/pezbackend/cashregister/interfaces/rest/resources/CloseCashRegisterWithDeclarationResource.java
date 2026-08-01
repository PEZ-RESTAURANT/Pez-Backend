package com.pezbackend.cashregister.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * DTO para el request de cierre de caja declarando un monto.
 */
public record CloseCashRegisterWithDeclarationResource(
        BigDecimal declaredAmount
) {}
