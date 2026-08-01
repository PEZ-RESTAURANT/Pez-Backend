package com.pezbackend.inventory.domain.services;

import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.domain.model.entities.Supply;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de consultas para insumos, movimientos y alertas de inventario.
 */
public interface SupplyQueryService {

    /**
     * Obtiene la lista completa de insumos registrados.
     *
     * @return lista de insumos
     */
    List<Supply> getAllSupplies();

    /**
     * Busca un insumo por su ID.
     *
     * @param id ID del insumo
     * @return el insumo si existe
     */
    Optional<Supply> getSupplyById(Long id);

    /**
     * Obtiene el historial de movimientos de un insumo.
     *
     * @param supplyId ID del insumo
     * @return lista de movimientos de stock
     */
    List<StockMovement> getMovementsForSupply(Long supplyId);

    /**
     * Obtiene la lista de insumos que se encuentran por debajo de su umbral mínimo.
     *
     * @return lista de insumos con stock bajo
     */
    List<Supply> getLowStockSupplies();

    /**
     * Obtiene la lista de insumos con descuadre de inventario (stock negativo).
     *
     * @return lista de insumos con stock negativo
     */
    List<Supply> getMismatchedSupplies();
}
