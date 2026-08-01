package com.pezbackend.iam.domain.model.commands;

/**
 * Comando para eliminar una anulación (override) de permiso para un usuario.
 */
public record DeletePermissionOverrideCommand(
        Long userId,
        String permissionCode,
        String adminUsername
) {
    /**
     * Valida los datos obligatorios del comando en su constructor compacto.
     */
    public DeletePermissionOverrideCommand {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        if (permissionCode == null || permissionCode.isBlank()) {
            throw new IllegalArgumentException("El código del permiso no puede estar vacío");
        }
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalArgumentException("El identificador del administrador no puede estar vacío");
        }
    }
}
