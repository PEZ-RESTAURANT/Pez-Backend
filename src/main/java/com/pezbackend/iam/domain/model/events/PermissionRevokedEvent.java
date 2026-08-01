package com.pezbackend.iam.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento de dominio emitido al revocar un override explícito de permiso (REVOKED o eliminación de override) a un usuario.
 * <p>
 * Implementa la interfaz {@link DomainEvent} para permitir el registro de auditoría automático.
 * </p>
 */
public record PermissionRevokedEvent(
        String userId,
        String targetUserId,
        String permissionCode,
        String reason,
        LocalDateTime timestamp
) implements DomainEvent {

    /**
     * Constructor conveniente con fecha y hora autogenerada.
     */
    public PermissionRevokedEvent(String userId, String targetUserId, String permissionCode, String reason) {
        this(userId, targetUserId, permissionCode, reason, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "PermissionRevoked";
    }

    @Override
    public String module() {
        return "iam";
    }

    @Override
    public Object payload() {
        return Map.of(
                "targetUserId", targetUserId,
                "permissionCode", permissionCode
        );
    }
}
