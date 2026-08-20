package com.pezbackend.kitchen.interfaces.rest.resources;

/**
 * Recurso DTO para recibir la solicitud de creación de una zona de cocina.
 *
 * @param name nombre descriptivo de la zona
 * @param printingEnabled indicador de si las comandas de esta zona se imprimen en ticket físico
 */
public record CreateKitchenZoneResource(String name, Boolean printingEnabled) {}
