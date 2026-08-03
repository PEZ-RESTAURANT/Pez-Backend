package com.pezbackend.catalog.domain.services;

/**
 * Servicio de comandos para gestionar la asignación de productos a zonas de cocina.
 */
public interface ProductKitchenZoneCommandService {

    /**
     * Asigna un producto a una zona de cocina. Si el producto ya tenía una zona, se actualiza.
     *
     * @param productId ID del producto a asignar
     * @param zoneId    ID de la zona de cocina
     */
    void assignProductToZone(Long productId, Long zoneId);

    /**
     * Elimina la asignación de zona de cocina de un producto.
     *
     * @param productId ID del producto
     */
    void removeProductFromZone(Long productId);
}
