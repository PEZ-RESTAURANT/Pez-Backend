package com.pezbackend.iam.domain.services;

/**
 * Servicio encargado de resolver si un usuario posee un permiso efectivo en el sistema.
 * <p>
 * Combina las configuraciones por defecto asociadas a los roles del usuario con las anulaciones
 * individuales (overrides) configuradas para su cuenta específica.
 * </p>
 */
public interface PermissionResolutionService {

    /**
     * Resuelve si un usuario cuenta con un permiso efectivo basándose en la jerarquía y precedencia:
     * <ol>
     *     <li>Lógica hardcoded especial para "permissions.manage": Solo disponible para el rol ADMIN.</li>
     *     <li>Anulación individual (AccountPermissionOverride): Si existe, prevalece su valor (GRANTED/REVOKED).</li>
     *     <li>Valores por defecto del rol (RolePermissionDefault): Si posee múltiples roles, se aplica lógica OR (se concede si algún rol lo tiene habilitado).</li>
     * </ol>
     *
     * @param userId         identificador del usuario a consultar
     * @param permissionCode código del permiso a validar (ej. "orders.cancel_item")
     * @return true si el usuario tiene el permiso concedido, false en caso contrario
     */
    boolean hasPermission(Long userId, String permissionCode);
}
