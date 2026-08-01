package com.pezbackend.iam.domain.services;

import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.domain.model.entities.AccountPermissionOverride;
import com.pezbackend.iam.domain.model.queries.ResolvedPermission;

import java.util.List;

/**
 * Servicio que define las consultas (queries) relacionadas con el catálogo de permisos.
 */
public interface PermissionQueryService {

    /**
     * Obtiene el catálogo completo de permisos del sistema.
     *
     * @return lista de todos los permisos registrados
     */
    List<Permission> getAllPermissions();

    /**
     * Obtiene el listado completo de anulaciones (overrides) individuales registradas para un usuario.
     *
     * @param userId identificador del usuario
     * @return lista de anulaciones del usuario
     */
    List<AccountPermissionOverride> getOverridesByUserId(Long userId);

    /**
     * Resuelve y calcula la lista de todos los permisos del catálogo y si están efectivamente concedidos o no para un usuario.
     *
     * @param userId identificador del usuario
     * @return lista de permisos resueltos con su estado de concesión final
     */
    List<ResolvedPermission> getEffectivePermissionsForUser(Long userId);
}
