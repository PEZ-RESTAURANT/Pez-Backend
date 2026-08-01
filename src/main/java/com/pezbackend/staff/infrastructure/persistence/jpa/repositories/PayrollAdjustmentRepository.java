package com.pezbackend.staff.infrastructure.persistence.jpa.repositories;

import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para gestionar la persistencia de los ajustes y descuentos de nómina.
 */
@Repository
public interface PayrollAdjustmentRepository extends JpaRepository<PayrollAdjustment, Long> {
    List<PayrollAdjustment> findAllByStaffProfileId(Long staffProfileId);
}
