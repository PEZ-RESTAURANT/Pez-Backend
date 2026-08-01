package com.pezbackend.kitchen.interfaces.rest.resources;

/**
 * Recurso DTO que representa los datos de una zona de cocina devueltos en las respuestas HTTP.
 *
 * @param id   ID de la zona de cocina
 * @param name nombre de la zona
 */
public record KitchenZoneResource(Long id, String name) {}
