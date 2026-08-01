package com.pezbackend.kitchen.domain.services;

import com.pezbackend.kitchen.domain.model.entities.KitchenZone;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de consultas para obtener información sobre zonas de cocina.
 */
public interface KitchenZoneQueryService {

    /**
     * Obtiene todas las zonas de cocina registradas.
     *
     * @return lista de zonas de cocina
     */
    List<KitchenZone> getAllZones();

    /**
     * Obtiene una zona de cocina por su ID único.
     *
     * @param id ID de la zona
     * @return la zona encontrada, opcionalmente
     */
    Optional<KitchenZone> getZoneById(Long id);
}
