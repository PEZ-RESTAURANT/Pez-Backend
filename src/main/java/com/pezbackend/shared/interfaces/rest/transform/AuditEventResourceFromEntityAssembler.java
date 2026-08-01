package com.pezbackend.shared.interfaces.rest.transform;

import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.interfaces.rest.resources.AuditEventResource;

/**
 * Ensamblador responsable de mapear las entidades de persistencia {@link AuditEvent}
 * a su correspondiente recurso DTO {@link AuditEventResource}.
 */
public class AuditEventResourceFromEntityAssembler {

    /**
     * Transforma una instancia de {@link AuditEvent} en un {@link AuditEventResource}.
     *
     * @param entity la entidad de auditoría a transformar
     * @return el recurso DTO resultante listo para ser serializado a JSON
     */
    public static AuditEventResource toResourceFromEntity(AuditEvent entity) {
        return new AuditEventResource(
                entity.getId(),
                entity.getEventType(),
                entity.getModule(),
                entity.getUserId(),
                entity.getDeviceId(),
                entity.getPayload(),
                entity.getReason(),
                entity.getTimestamp()
        );
    }
}
