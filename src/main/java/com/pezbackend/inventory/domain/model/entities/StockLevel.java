package com.pezbackend.inventory.domain.model.entities;

/**
 * Estados de stock escalonados para control de inventario y alertas.
 */
public enum StockLevel {
    AGOTADO,
    CRITICO,
    BAJO,
    ESTABLE
}
