package com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories;

import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para gestionar la persistencia del historial de transacciones de puntos.
 */
@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findAllByCustomerId(Long customerId);
}
