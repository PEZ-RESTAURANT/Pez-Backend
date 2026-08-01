package com.pezbackend.catalog.interfaces.rest.resources;

/**
 * Recurso DTO para recibir la asignación de una zona de cocina a un producto.
 *
 * @param zoneId ID de la zona de cocina
 */
public record AssignProductToZoneResource(Long zoneId) {}
