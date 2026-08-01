package com.pezbackend.inventory.infrastructure.persistence.jpa.repositories;

import com.pezbackend.inventory.domain.model.entities.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para acceder a los datos de los insumos (Supply).
 */
@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {

    /**
     * Busca un insumo por su nombre de forma insensible a mayúsculas y minúsculas.
     *
     * @param name nombre del insumo
     * @return el insumo encontrado, si existe
     */
    Optional<Supply> findByNameIgnoreCase(String name);

    /**
     * Comprueba si existe un insumo con un nombre determinado, de forma insensible a mayúsculas y minúsculas.
     *
     * @param name nombre del insumo
     * @return true si ya existe, false en caso contrario
     */
    boolean existsByNameIgnoreCase(String name);
}
