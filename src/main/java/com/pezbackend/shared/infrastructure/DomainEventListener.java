package com.pezbackend.shared.infrastructure;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.domain.model.DomainEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Escucha los eventos de dominio del sistema y se encarga de transformarlos
 * y persistirlos en el log histórico de auditoría.
 * <p>
 * Se ejecuta únicamente si la transacción actual ha finalizado con éxito (phase = AFTER_COMMIT),
 * evitando registrar acciones de negocio que terminaron en un rollback.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventListener {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Escucha cualquier evento que implemente la interfaz {@link DomainEvent} y guarda
     * un registro equivalente de auditoría de manera asíncrona/desacoplada tras el commit.
     *
     * @param event el evento de dominio emitido por la aplicación
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDomainEvent(DomainEvent event) {
        log.info("Auditoría: capturado evento '{}' del módulo '{}'", event.eventType(), event.module());
        
        Long currentTenantId = TenantContext.getCurrentTenantId();
        boolean contextSetTemporarily = false;
        
        if (currentTenantId == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
                currentTenantId = userDetails.getRestaurantId();
                if (currentTenantId != null) {
                    TenantContext.setCurrentTenantId(currentTenantId);
                    contextSetTemporarily = true;
                }
            }
        }

        try {
            Map<String, Object> payloadMap;
            Object rawPayload = event.payload();

            if (rawPayload instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> casted = (Map<String, Object>) rawPayload;
                payloadMap = casted;
            } else if (rawPayload != null) {
                payloadMap = objectMapper.convertValue(rawPayload, new TypeReference<Map<String, Object>>() {});
            } else {
                payloadMap = Map.of();
            }

            AuditEvent auditEvent = new AuditEvent(
                    event.eventType(),
                    event.module(),
                    event.userId(),
                    event.deviceId(),
                    payloadMap,
                    event.reason(),
                    event.timestamp()
            );

            auditEventRepository.save(auditEvent);
            log.debug("Auditoría: evento '{}' guardado con ID {}", event.eventType(), auditEvent.getId());
        } catch (Exception ex) {
            log.error("Auditoría: Error al persistir el evento '{}': {}", event.eventType(), ex.getMessage(), ex);
        } finally {
            if (contextSetTemporarily) {
                TenantContext.clear();
            }
        }
    }
}
