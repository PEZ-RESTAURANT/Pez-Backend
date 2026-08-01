package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Define el tipo o canal del pedido realizado.
 */
public enum OrderType {
    /**
     * Consumo en el salón físico, asociado obligatoriamente a una mesa.
     */
    DINE_IN,

    /**
     * Pedido para llevar (para recoger en el local).
     */
    TAKEAWAY,

    /**
     * Pedido para entrega a domicilio.
     */
    DELIVERY
}
