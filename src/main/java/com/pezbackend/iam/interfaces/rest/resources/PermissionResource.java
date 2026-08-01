package com.pezbackend.iam.interfaces.rest.resources;

/**
 * Representa la información básica de un permiso en formato DTO.
 *
 * @param id          ID del permiso
 * @param code        código del permiso (ej. "orders.create")
 * @param module      módulo al que pertenece
 * @param description descripción descriptiva
 */
public record PermissionResource(
        Long id,
        String code,
        String module,
        String description
) {}
