package com.pezbackend.kitchen.interfaces.rest.resources;

/**
 * Recurso DTO para recibir la solicitud de creación de una zona de cocina.
 *
 * @param name nombre descriptivo de la zona
 */
public record CreateKitchenZoneResource(String name) {}
