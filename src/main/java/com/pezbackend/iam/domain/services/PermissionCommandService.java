package com.pezbackend.iam.domain.services;

import com.pezbackend.iam.domain.model.commands.CreatePermissionOverrideCommand;
import com.pezbackend.iam.domain.model.commands.DeletePermissionOverrideCommand;

/**
 * Servicio que define las operaciones de escritura (comandos) para la gestión de permisos.
 */
public interface PermissionCommandService {

    /**
     * Procesa la creación o actualización de una anulación (override) de permiso para un usuario.
     *
     * @param command comando con los datos de la anulación
     */
    void handle(CreatePermissionOverrideCommand command);

    /**
     * Procesa la eliminación de una anulación (override) de permiso para un usuario.
     *
     * @param command comando con la información de la anulación a eliminar
     */
    void handle(DeletePermissionOverrideCommand command);
}
