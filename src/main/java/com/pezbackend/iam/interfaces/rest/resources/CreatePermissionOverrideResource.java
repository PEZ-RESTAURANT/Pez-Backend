package com.pezbackend.iam.interfaces.rest.resources;

import com.pezbackend.iam.domain.model.valueobjects.OverrideValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Recurso de entrada (DTO) para registrar o actualizar una anulación de permiso.
 *
 * @param permissionCode código de permiso a anular (ej. "orders.cancel_item")
 * @param value          el valor de la anulación (GRANTED o REVOKED)
 * @param reason         motivo de la anulación (opcional)
 */
public record CreatePermissionOverrideResource(
        @NotBlank(message = "El código de permiso es requerido")
        String permissionCode,

        @NotNull(message = "El valor de la anulación (value) es requerido (GRANTED o REVOKED)")
        OverrideValue value,

        String reason
) {}
