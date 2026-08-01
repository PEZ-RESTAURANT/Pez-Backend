package com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories;

import com.pezbackend.cashregister.domain.model.entities.CashRegisterMismatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad CashRegisterMismatch.
 */
@Repository
public interface CashRegisterMismatchRepository extends JpaRepository<CashRegisterMismatch, Long> {
}
