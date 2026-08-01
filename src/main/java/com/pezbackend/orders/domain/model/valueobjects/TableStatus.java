package com.pezbackend.orders.domain.model.valueobjects;

/**
 * Representa la máquina de estados y ciclo de vida de una mesa en el salón.
 */
public enum TableStatus {
    /**
     * Mesa vacía y disponible para ser ocupada.
     */
    FREE,

    /**
     * Los clientes se han sentado y solicitado atención, pero aún no han sido atendidos.
     */
    UNATTENDED,

    /**
     * El mozo está en proceso de tomar el pedido de los clientes.
     */
    TAKING_ORDER,

    /**
     * El pedido ha sido enviado a la cocina y los clientes están esperando sus platos.
     */
    WAITING_DISHES,

    /**
     * Todos los platos solicitados en la comanda han sido preparados y entregados.
     */
    ALL_DELIVERED,

    /**
     * Se ha emitido la precuenta y el comprobante de venta, pero la comanda aún no ha sido pagada.
     */
    ISSUED_UNPAID,

    /**
     * La comanda ha sido pagada en caja.
     */
    PAID
}
