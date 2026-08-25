package com.pezbackend.staff.infrastructure.persistence.jpa.repositories;

import com.pezbackend.staff.domain.model.entities.UnmappedFingerprintEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para gestionar la persistencia de UnmappedFingerprintEvent.
 */
@Repository
public interface UnmappedFingerprintEventRepository extends JpaRepository<UnmappedFingerprintEvent, Long> {
}
