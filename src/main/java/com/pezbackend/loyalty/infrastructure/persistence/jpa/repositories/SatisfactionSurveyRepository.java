package com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories;

import com.pezbackend.loyalty.domain.model.entities.SatisfactionSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para gestionar la persistencia de las encuestas de satisfacción.
 */
@Repository
public interface SatisfactionSurveyRepository extends JpaRepository<SatisfactionSurvey, Long> {
    List<SatisfactionSurvey> findAllByCustomerId(Long customerId);
}
