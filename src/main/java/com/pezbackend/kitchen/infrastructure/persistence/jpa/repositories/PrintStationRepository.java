package com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories;

import com.pezbackend.kitchen.domain.model.entities.PrintStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrintStationRepository extends JpaRepository<PrintStation, Long> {
    Optional<PrintStation> findByName(String name);
    boolean existsByName(String name);
}
