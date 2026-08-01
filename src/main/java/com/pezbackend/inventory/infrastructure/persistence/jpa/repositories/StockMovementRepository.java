package com.pezbackend.inventory.infrastructure.persistence.jpa.repositories;

import com.pezbackend.inventory.domain.model.entities.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para acceder a las transacciones de movimientos de stock.
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /**
     * Recupera todos los movimientos de stock asociados a un insumo, ordenados cronológicamente de forma descendente.
     *
     * @param supplyId ID del insumo
     * @return lista de movimientos de stock
     */
    List<StockMovement> findAllBySupplyIdOrderByDateDesc(Long supplyId);
}
