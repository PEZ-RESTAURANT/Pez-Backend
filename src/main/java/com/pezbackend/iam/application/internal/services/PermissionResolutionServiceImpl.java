package com.pezbackend.iam.application.internal.services;

import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.AccountPermissionOverride;
import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.domain.model.valueobjects.OverrideValue;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.domain.services.PermissionResolutionService;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.AccountPermissionOverrideRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PermissionRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RolePermissionDefaultRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link PermissionResolutionService}.
 * <p>
 * Resuelve permisos granulares evaluando anulaciones individuales (overrides) y reglas de roles por defecto (OR).
 * </p>
 */
@Service("permissionResolutionService")
@RequiredArgsConstructor
public class PermissionResolutionServiceImpl implements PermissionResolutionService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final AccountPermissionOverrideRepository overrideRepository;
    private final RolePermissionDefaultRepository roleDefaultRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permissionCode) {
        // 1. Validar la existencia del usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "USER_NOT_FOUND",
                        "No se encontró el usuario con ID: " + userId
                ));

        // 2. Si la cuenta es ADMIN, retornar true inmediatamente (bypass total de seguridad)
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Roles.ADMIN);
        if (isAdmin) {
            return true;
        }

        // 3. Validar la existencia del permiso en el catálogo
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PERMISSION_NOT_FOUND",
                        "No se encontró el permiso con código: " + permissionCode
                ));

        // 4. Regla especial hardcoded: 'permissions.manage'
        if ("permissions.manage".equals(permissionCode)) {
            // Solo usuarios con el rol ADMIN pueden gestionar permisos. No admite overrides.
            return true; // Ya sabemos que isAdmin es false si llegó acá, pero por completitud de firma
        }

        // 4. Evaluar override individual de la cuenta
        Optional<AccountPermissionOverride> overrideOpt = overrideRepository.findByUserIdAndPermissionId(userId, permission.getId());
        if (overrideOpt.isPresent()) {
            return overrideOpt.get().getValue() == OverrideValue.GRANTED;
        }

        // 5. Evaluar la configuración por defecto del rol (Lógica OR si el usuario tiene múltiples roles)
        List<Roles> userRoles = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();

        if (userRoles.isEmpty()) {
            return false;
        }

        return roleDefaultRepository.existsByPermissionIdAndRoleInAndGrantedTrue(permission.getId(), userRoles);
    }
}
