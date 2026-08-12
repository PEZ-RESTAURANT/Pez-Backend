package com.pezbackend.iam.domain.model.queries;

/**
 * Representa el estado resuelto final (efectivo) de un permiso para un usuario específico.
 *
 * @param code        código único del permiso (ej. "orders.create")
 * @param module      módulo al que pertenece
 * @param description descripción textual de la acción permitida
 * @param granted     indica si el permiso está finalmente habilitado para el usuario
 */
public record ResolvedPermission(
        String code,
        String module,
        String description,
        boolean granted,
        boolean isOverride,
        boolean roleDefaultValue,
        String overrideGrantedBy,
        java.time.LocalDateTime overrideDate,
        String overrideReason
) {}
