package com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories;

import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para acceder a los datos de las zonas de cocina.
 */
@Repository
public interface KitchenZoneRepository extends JpaRepository<KitchenZone, Long> {

    /**
     * Busca una zona de cocina por su nombre.
     *
     * @param name nombre de la zona
     * @return la zona encontrada, si existe
     */
    Optional<KitchenZone> findByName(String name);

    /**
     * Comprueba si existe una zona de cocina con un nombre específico.
     *
     * @param name nombre de la zona
     * @return true si ya existe, false en caso contrario
     */
    boolean existsByName(String name);
}
