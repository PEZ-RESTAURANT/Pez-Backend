package com.pezbackend.billing.domain.services;

/**
 * Representa el resultado obtenido de una consulta externa de DNI o RUC.
 */
public record LookupResult(
        String documentNumber,
        String name,
        String address,
        boolean success
) {
    public static LookupResult failed() {
        return new LookupResult(null, null, null, false);
    }
}
