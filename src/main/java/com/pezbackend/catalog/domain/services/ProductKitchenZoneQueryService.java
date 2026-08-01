package com.pezbackend.catalog.domain.services;

import java.util.Optional;
import java.util.Set;

/**
 * Servicio de consultas para obtener información sobre la asignación de productos a zonas de cocina.
 */
public interface ProductKitchenZoneQueryService {

    /**
     * Obtiene el ID de la zona de cocina asignada a un producto.
     *
     * @param productId ID del producto
     * @return el ID de la zona de cocina, si está asignado
     */
    Optional<Long> getZoneIdForProduct(Long productId);

    /**
     * Obtiene los IDs de todos los productos asignados a una zona de cocina.
     *
     * @param zoneId ID de la zona de cocina
     * @return conjunto de IDs de productos asociados a la zona
     */
    Set<Long> getProductIdsForZone(Long zoneId);
}
