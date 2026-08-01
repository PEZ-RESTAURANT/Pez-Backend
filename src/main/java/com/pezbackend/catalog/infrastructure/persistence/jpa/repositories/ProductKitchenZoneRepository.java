package com.pezbackend.catalog.infrastructure.persistence.jpa.repositories;

import com.pezbackend.catalog.domain.model.entities.ProductKitchenZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para acceder a los datos de la relación de productos y zonas de cocina.
 */
@Repository
public interface ProductKitchenZoneRepository extends JpaRepository<ProductKitchenZone, Long> {

    /**
     * Busca la asignación de zona de cocina de un producto específico.
     *
     * @param productId ID del producto
     * @return la asignación encontrada, si existe
     */
    Optional<ProductKitchenZone> findByProductId(Long productId);

    /**
     * Busca todas las asignaciones de productos para una zona de cocina específica.
     *
     * @param zoneId ID de la zona de cocina
     * @return lista de asignaciones de productos a la zona
     */
    List<ProductKitchenZone> findAllByZoneId(Long zoneId);
}
