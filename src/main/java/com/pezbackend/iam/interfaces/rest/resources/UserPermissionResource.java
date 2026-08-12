package com.pezbackend.iam.interfaces.rest.resources;

/**
 * DTO para representar el estado efectivo de un permiso resuelto para un usuario.
 *
 * @param code        código del permiso (ej. "orders.create")
 * @param module      módulo al que pertenece
 * @param description descripción del permiso
 * @param granted     indica si el usuario posee efectivamente el permiso
 */
public record UserPermissionResource(
        String code,
        String module,
        String description,
        boolean granted,
        boolean isOverride,
        boolean roleDefaultValue,
        String overrideGrantedBy,
        String overrideDate,
        String overrideReason
) {}
