package com.pezbackend.iam.application.internal.services;

import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.commands.CreatePermissionOverrideCommand;
import com.pezbackend.iam.domain.model.commands.DeletePermissionOverrideCommand;
import com.pezbackend.iam.domain.model.entities.AccountPermissionOverride;
import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.domain.model.events.PermissionGrantedEvent;
import com.pezbackend.iam.domain.model.events.PermissionRevokedEvent;
import com.pezbackend.iam.domain.model.valueobjects.OverrideValue;
import com.pezbackend.iam.domain.services.PermissionCommandService;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.AccountPermissionOverrideRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PermissionRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación de {@link PermissionCommandService}.
 * <p>
 * Gestiona el ciclo de vida de los overrides de permisos de usuarios y publica eventos de dominio asociados.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionCommandServiceImpl implements PermissionCommandService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final AccountPermissionOverrideRepository overrideRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void handle(CreatePermissionOverrideCommand command) {
        String code = command.permissionCode();
        log.info("Procesando creación de override para usuario ID {} y permiso '{}'", command.userId(), code);

        // 1. Regla especial: No se puede hacer override del permiso 'permissions.manage'
        if ("permissions.manage".equals(code)) {
            throw new BusinessRuleViolationException(
                    "CANNOT_OVERRIDE_MANAGE_PERMISSION",
                    "No se permite crear anulaciones manuales sobre el permiso crítico 'permissions.manage'"
            );
        }

        // 2. Validar que el usuario existe
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "USER_NOT_FOUND",
                        "No se encontró el usuario con ID: " + command.userId()
                ));

        // 3. Validar que el permiso existe en el catálogo
        Permission permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PERMISSION_NOT_FOUND",
                        "No se encontró el permiso con código: " + code
                ));

        // 4. Crear o actualizar override
        AccountPermissionOverride override = overrideRepository
                .findByUserIdAndPermissionId(user.getId(), permission.getId())
                .orElse(null);

        if (override == null) {
            override = new AccountPermissionOverride(
                    user.getId(),
                    permission,
                    command.value(),
                    command.adminUsername(),
                    command.reason()
            );
        } else {
            override.setValue(command.value());
            override.setGrantedBy(command.adminUsername());
            override.setDate(LocalDateTime.now());
            override.setReason(command.reason());
        }

        overrideRepository.save(override);
        log.info("Override guardado. Usuario ID: {}, Permiso: {}, Valor: {}",
                user.getId(), code, command.value());

        // 5. Publicar evento de dominio para auditoría automática
        if (command.value() == OverrideValue.GRANTED) {
            eventPublisher.publishEvent(new PermissionGrantedEvent(
                    command.adminUsername(),
                    String.valueOf(user.getId()),
                    code,
                    command.reason()
            ));
        } else {
            eventPublisher.publishEvent(new PermissionRevokedEvent(
                    command.adminUsername(),
                    String.valueOf(user.getId()),
                    code,
                    command.reason()
            ));
        }
    }

    @Override
    @Transactional
    public void handle(DeletePermissionOverrideCommand command) {
        String code = command.permissionCode();
        log.info("Procesando eliminación de override para usuario ID {} y permiso '{}'", command.userId(), code);

        // 1. Regla especial: No se puede hacer override (ni borrarlo) del permiso 'permissions.manage'
        if ("permissions.manage".equals(code)) {
            throw new BusinessRuleViolationException(
                    "CANNOT_OVERRIDE_MANAGE_PERMISSION",
                    "No se permite gestionar anulaciones sobre el permiso crítico 'permissions.manage'"
            );
        }

        // 2. Validar que el usuario existe
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "USER_NOT_FOUND",
                        "No se encontró el usuario con ID: " + command.userId()
                ));

        // 3. Validar que el permiso existe
        Permission permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PERMISSION_NOT_FOUND",
                        "No se encontró el permiso con código: " + code
                ));

        // 4. Buscar y eliminar override
        AccountPermissionOverride override = overrideRepository
                .findByUserIdAndPermissionId(user.getId(), permission.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OVERRIDE_NOT_FOUND",
                        "No se encontró ninguna anulación para el usuario ID " + user.getId() + " y permiso " + code
                ));

        overrideRepository.delete(override);
        log.info("Override eliminado para usuario ID {} y permiso '{}'", user.getId(), code);

        // 5. Publicar evento de dominio de revocación de override
        eventPublisher.publishEvent(new PermissionRevokedEvent(
                command.adminUsername(),
                String.valueOf(user.getId()),
                code,
                "Anulación eliminada por el administrador"
        ));
    }
}
