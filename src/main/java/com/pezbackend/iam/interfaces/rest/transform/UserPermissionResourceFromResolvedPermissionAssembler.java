package com.pezbackend.iam.interfaces.rest.transform;

import com.pezbackend.iam.domain.model.queries.ResolvedPermission;
import com.pezbackend.iam.interfaces.rest.resources.UserPermissionResource;

/**
 * Ensamblador responsable de transformar objetos {@link ResolvedPermission}
 * a su correspondiente recurso DTO {@link UserPermissionResource}.
 */
public class UserPermissionResourceFromResolvedPermissionAssembler {

    /**
     * Mapea un permiso resuelto a su DTO de salida.
     *
     * @param resolved permiso resuelto del usuario
     * @return recurso UserPermissionResource
     */
    public static UserPermissionResource toResourceFromResolved(ResolvedPermission resolved) {
        return new UserPermissionResource(
                resolved.code(),
                resolved.module(),
                resolved.description(),
                resolved.granted(),
                resolved.isOverride(),
                resolved.roleDefaultValue(),
                resolved.overrideGrantedBy(),
                resolved.overrideDate() != null ? resolved.overrideDate().toString() : null,
                resolved.overrideReason()
            );
    }
}
