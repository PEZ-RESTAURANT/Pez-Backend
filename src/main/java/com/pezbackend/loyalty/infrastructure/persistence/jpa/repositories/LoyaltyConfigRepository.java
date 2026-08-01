package com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories;

import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para gestionar la configuración de fidelización.
 */
@Repository
public interface LoyaltyConfigRepository extends JpaRepository<LoyaltyConfig, Long> {
}
