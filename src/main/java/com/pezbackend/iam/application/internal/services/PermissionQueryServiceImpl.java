package com.pezbackend.iam.application.internal.services;

import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.AccountPermissionOverride;
import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.domain.model.queries.ResolvedPermission;
import com.pezbackend.iam.domain.services.PermissionQueryService;
import com.pezbackend.iam.domain.services.PermissionResolutionService;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.AccountPermissionOverrideRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PermissionRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link PermissionQueryService}.
 * <p>
 * Realiza consultas del catálogo de permisos y calcula los permisos efectivos de los usuarios.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionQueryServiceImpl implements PermissionQueryService {

    private final PermissionRepository permissionRepository;
    private final AccountPermissionOverrideRepository overrideRepository;
    private final UserRepository userRepository;
    private final PermissionResolutionService permissionResolutionService;

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountPermissionOverride> getOverridesByUserId(Long userId) {
        // Validar existencia del usuario
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "USER_NOT_FOUND",
                    "No se encontró el usuario con ID: " + userId
            );
        }
        return overrideRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResolvedPermission> getEffectivePermissionsForUser(Long userId) {
        // Validar existencia del usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "USER_NOT_FOUND",
                        "No se encontró el usuario con ID: " + userId
                ));

        List<Permission> allPermissions = permissionRepository.findAll();

        return allPermissions.stream()
                .map(permission -> {
                    boolean granted = permissionResolutionService.hasPermission(user.getId(), permission.getCode());
                    return new ResolvedPermission(
                            permission.getCode(),
                            permission.getModule(),
                            permission.getDescription(),
                            granted
                    );
                })
                .toList();
    }
}
