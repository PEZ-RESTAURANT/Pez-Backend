package com.pezbackend.iam.interfaces.rest;

import com.pezbackend.iam.domain.model.commands.CreatePermissionOverrideCommand;
import com.pezbackend.iam.domain.model.commands.DeletePermissionOverrideCommand;
import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.domain.model.queries.ResolvedPermission;
import com.pezbackend.iam.domain.services.PermissionCommandService;
import com.pezbackend.iam.domain.services.PermissionQueryService;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.AuthorizeRoles;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.iam.interfaces.rest.resources.CreatePermissionOverrideResource;
import com.pezbackend.iam.interfaces.rest.resources.PermissionResource;
import com.pezbackend.iam.interfaces.rest.resources.UserPermissionResource;
import com.pezbackend.iam.interfaces.rest.transform.PermissionResourceFromEntityAssembler;
import com.pezbackend.iam.interfaces.rest.transform.UserPermissionResourceFromResolvedPermissionAssembler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión del catálogo de permisos, asignación de anulaciones (overrides)
 * y consulta de permisos efectivos de los usuarios.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class PermissionsController {

    private final PermissionQueryService queryService;
    private final PermissionCommandService commandService;

    /**
     * Recupera el catálogo completo de permisos del local.
     * Acceso permitido a cualquier usuario autenticado en el sistema.
     *
     * @return listado de todos los permisos definidos
     */
    @PreAuthorize(AuthorizeRoles.ANY_AUTHENTICATED)
    @GetMapping("/api/v1/permissions")
    public ResponseEntity<List<PermissionResource>> getAllPermissions() {
        log.debug("Procesando consulta del catálogo de permisos...");
        List<Permission> permissions = queryService.getAllPermissions();
        List<PermissionResource> resources = permissions.stream()
                .map(PermissionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Consulta los permisos efectivos ya resueltos para un usuario específico.
     * Acceso restringido a administradores con el permiso 'permissions.manage'.
     *
     * @param userId identificador del usuario a consultar
     * @return listado de permisos efectivos del usuario
     */
    @RequiresPermission("permissions.manage")
    @GetMapping("/api/v1/users/{userId}/permissions")
    public ResponseEntity<List<UserPermissionResource>> getEffectivePermissionsForUser(@PathVariable Long userId) {
        log.info("Procesando consulta de permisos efectivos para usuario ID: {}", userId);
        List<ResolvedPermission> resolved = queryService.getEffectivePermissionsForUser(userId);
        List<UserPermissionResource> resources = resolved.stream()
                .map(UserPermissionResourceFromResolvedPermissionAssembler::toResourceFromResolved)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Registra o actualiza una anulación (override) de permiso para un usuario específico.
     * Acceso restringido a administradores con el permiso 'permissions.manage'.
     *
     * @param userId   identificador del usuario al que se le aplicará el override
     * @param resource datos del override (código de permiso, valor GRANTED/REVOKED, y justificación)
     * @return respuesta vacía HTTP 200 OK
     */
    @RequiresPermission("permissions.manage")
    @PostMapping("/api/v1/users/{userId}/permissions/override")
    public ResponseEntity<Void> createOrUpdateOverride(
            @PathVariable Long userId,
            @Valid @RequestBody CreatePermissionOverrideResource resource
    ) {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Administrador '{}' solicita override de permiso '{}' con valor '{}' para usuario ID '{}'",
                adminUsername, resource.permissionCode(), resource.value(), userId);

        CreatePermissionOverrideCommand command = new CreatePermissionOverrideCommand(
                userId,
                resource.permissionCode(),
                resource.value(),
                resource.reason(),
                adminUsername
        );

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina una anulación (override) de permiso de un usuario específico, restaurando el valor por defecto de su rol.
     * Acceso restringido a administradores con el permiso 'permissions.manage'.
     *
     * @param userId         identificador del usuario
     * @param permissionCode código del permiso cuya anulación se desea eliminar
     * @return respuesta vacía HTTP 200 OK
     */
    @RequiresPermission("permissions.manage")
    @DeleteMapping("/api/v1/users/{userId}/permissions/override/{permissionCode}")
    public ResponseEntity<Void> deleteOverride(
            @PathVariable Long userId,
            @PathVariable String permissionCode
    ) {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Administrador '{}' solicita eliminar override de permiso '{}' para usuario ID '{}'",
                adminUsername, permissionCode, userId);

        DeletePermissionOverrideCommand command = new DeletePermissionOverrideCommand(
                userId,
                permissionCode,
                adminUsername
        );

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }
}
