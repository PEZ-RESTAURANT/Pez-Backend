package com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories;

import com.pezbackend.tenancy.domain.model.aggregates.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para administrar la persistencia de la entidad {@link Restaurant}.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
