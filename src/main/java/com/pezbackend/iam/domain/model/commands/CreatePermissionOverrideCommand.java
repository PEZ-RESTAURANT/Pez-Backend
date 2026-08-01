package com.pezbackend.iam.domain.model.commands;

import com.pezbackend.iam.domain.model.valueobjects.OverrideValue;

/**
 * Comando para registrar o actualizar una anulación (override) de permiso para un usuario.
 */
public record CreatePermissionOverrideCommand(
        Long userId,
        String permissionCode,
        OverrideValue value,
        String reason,
        String adminUsername
) {
    /**
     * Valida los datos obligatorios del comando en su constructor compacto.
     */
    public CreatePermissionOverrideCommand {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        if (permissionCode == null || permissionCode.isBlank()) {
            throw new IllegalArgumentException("El código del permiso no puede estar vacío");
        }
        if (value == null) {
            throw new IllegalArgumentException("El valor de la anulación no puede ser nulo");
        }
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalArgumentException("El identificador del administrador no puede estar vacío");
        }
    }
}
