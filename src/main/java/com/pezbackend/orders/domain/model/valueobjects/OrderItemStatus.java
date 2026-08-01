package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Representa los estados posibles por los que transiciona un plato o ítem de la comanda en cocina.
 */
public enum OrderItemStatus {
    /**
     * El ítem ha sido comandado pero aún no se inicia su preparación en cocina.
     */
    PENDING,

    /**
     * Cocina está preparando activamente el plato.
     */
    IN_PREPARATION,

    /**
     * El plato está listo en el mostrador de cocina y espera ser recogido por el mozo.
     */
    READY,

    /**
     * El mozo ha entregado el plato a la mesa del cliente.
     */
    DELIVERED
}
