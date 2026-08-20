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
     * @param printingEnabled si la impresión está habilitada
     * @return la zona creada
     */
    KitchenZone createZone(String name, boolean printingEnabled);

    /**
     * Modifica el nombre de una zona de cocina existente.
     *
     * @param id   ID de la zona
     * @param name nuevo nombre
     * @param printingEnabled si la impresión está habilitada
     * @return la zona actualizada
     */
    KitchenZone updateZone(Long id, String name, boolean printingEnabled);

    /**
     * Elimina una zona de cocina.
     *
     * @param id ID de la zona a eliminar
     */
    void deleteZone(Long id);
}
