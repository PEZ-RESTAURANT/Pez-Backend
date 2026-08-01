package com.pezbackend.iam.interfaces.rest.transform;

import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.interfaces.rest.resources.PermissionResource;

/**
 * Ensamblador responsable de transformar entidades {@link Permission}
 * a su correspondiente recurso DTO {@link PermissionResource}.
 */
public class PermissionResourceFromEntityAssembler {

    /**
     * Mapea un permiso a su DTO de salida.
     *
     * @param entity entidad Permission
     * @return recurso PermissionResource
     */
    public static PermissionResource toResourceFromEntity(Permission entity) {
        return new PermissionResource(
                entity.getId(),
                entity.getCode(),
                entity.getModule(),
                entity.getDescription()
        );
    }
}
