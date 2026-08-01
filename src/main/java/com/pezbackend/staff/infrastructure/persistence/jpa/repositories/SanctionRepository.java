package com.pezbackend.staff.infrastructure.persistence.jpa.repositories;

import com.pezbackend.staff.domain.model.entities.Sanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para gestionar la persistencia de las sanciones del personal.
 */
@Repository
public interface SanctionRepository extends JpaRepository<Sanction, Long> {
    List<Sanction> findAllByStaffProfileId(Long staffProfileId);
}
