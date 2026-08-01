package com.pezbackend.analytics.infrastructure.persistence.jpa.repositories;

import com.pezbackend.analytics.domain.model.entities.AnalyticsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para gestionar la configuración de analítica de negocio.
 */
@Repository
public interface AnalyticsConfigRepository extends JpaRepository<AnalyticsConfig, Long> {
}
