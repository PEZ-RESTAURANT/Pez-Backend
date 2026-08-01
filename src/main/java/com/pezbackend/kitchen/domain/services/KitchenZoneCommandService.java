package com.pezbackend.kitchen.domain.services;

import com.pezbackend.kitchen.domain.model.entities.KitchenZone;

/**
 * Servicio de comandos para gestionar la creación, modificación y eliminación de zonas de cocina.
 */
public interface KitchenZoneCommandService {

    /**
     * Crea una nueva zona de cocina.
     *
     * @param name nombre de la zona
     * @return la zona creada
     */
    KitchenZone createZone(String name);

    /**
     * Modifica el nombre de una zona de cocina existente.
     *
     * @param id   ID de la zona
     * @param name nuevo nombre
     * @return la zona actualizada
     */
    KitchenZone updateZone(Long id, String name);

    /**
     * Elimina una zona de cocina.
     *
     * @param id ID de la zona a eliminar
     */
    void deleteZone(Long id);
}
