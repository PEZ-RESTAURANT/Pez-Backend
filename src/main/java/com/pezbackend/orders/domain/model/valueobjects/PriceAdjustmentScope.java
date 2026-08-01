package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Define el alcance o cobertura de la aplicación de un ajuste o descuento en el precio.
 */
public enum PriceAdjustmentScope {
    /**
     * Aplica a un único plato o ítem de la comanda de forma particular.
     */
    INDIVIDUAL,

    /**
     * Aplica a un subgrupo de platos comandados.
     */
    GROUP,

    /**
     * Aplica al monto total global de toda la comanda.
     */
    ALL
}
