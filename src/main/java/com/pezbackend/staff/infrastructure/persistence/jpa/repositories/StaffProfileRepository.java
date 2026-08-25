package com.pezbackend.staff.infrastructure.persistence.jpa.repositories;

import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para gestionar la persistencia del agregado StaffProfile.
 */
@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    Optional<StaffProfile> findByAccountId(Long accountId);
    Optional<StaffProfile> findByFingerprintId(Integer fingerprintId);
}
