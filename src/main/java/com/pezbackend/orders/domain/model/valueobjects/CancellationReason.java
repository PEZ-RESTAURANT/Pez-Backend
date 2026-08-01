package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Motivos predefinidos para la cancelación o eliminación de ítems de una comanda.
 */
public enum CancellationReason {
    /**
     * Error al tomar el pedido (error del mozo).
     */
    WRONG_ORDER,

    /**
     * El cliente cambió de opinión sobre el plato.
     */
    CUSTOMER_CHANGED_MIND,

    /**
     * El plato demoró demasiado en cocina y el cliente solicitó retirarlo.
     */
    DISH_DELAYED,

    /**
     * Otro motivo (requiere justificación en texto libre obligatoria).
     */
    OTHER
}
