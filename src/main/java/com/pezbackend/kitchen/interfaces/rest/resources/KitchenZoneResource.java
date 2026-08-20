package com.pezbackend.kitchen.interfaces.rest.resources;

/**
 * Recurso DTO que representa los datos de una zona de cocina devueltos en las respuestas HTTP.
 *
 * @param id   ID de la zona de cocina
 * @param name nombre de la zona
 * @param printingEnabled indicador de si las comandas de esta zona se imprimen en ticket físico
 */
public record KitchenZoneResource(Long id, String name, boolean printingEnabled) {}
