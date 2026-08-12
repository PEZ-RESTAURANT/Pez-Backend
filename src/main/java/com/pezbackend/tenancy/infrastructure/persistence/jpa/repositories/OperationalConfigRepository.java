package com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories;

import com.pezbackend.tenancy.domain.model.entities.OperationalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para gestionar la configuración operativa general.
 */
@Repository
public interface OperationalConfigRepository extends JpaRepository<OperationalConfig, Long> {
}
