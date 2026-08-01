package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Representa el estado de una comanda (espejo de {@link TableStatus} para pedidos sin mesa).
 */
public enum OrderStatus {
    /**
     * Comanda creada pero aún sin atender.
     */
    FREE,

    /**
     * Pedido registrado y en cola de atención.
     */
    UNATTENDED,

    /**
     * En proceso de toma de comanda.
     */
    TAKING_ORDER,

    /**
     * Esperando la preparación y entrega de platos de cocina.
     */
    WAITING_DISHES,

    /**
     * Todos los ítems han sido entregados al cliente.
     */
    ALL_DELIVERED,

    /**
     * Comprobante emitido, pendiente de cobro en caja.
     */
    ISSUED_UNPAID,

    /**
     * Comanda pagada y finalizada.
     */
    PAID
}
