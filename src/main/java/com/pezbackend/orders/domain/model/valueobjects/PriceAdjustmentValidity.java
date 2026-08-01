package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Define la validez temporal del ajuste de precio aplicado.
 */
public enum PriceAdjustmentValidity {
    /**
     * El ajuste es permanente y no tiene fecha de caducidad.
     */
    PERMANENT,

    /**
     * El ajuste es temporal y se rige por un rango de fechas/horas de inicio y fin.
     */
    TEMPORARY
}
