package com.pezbackend.shared.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.domain.services.AuditQueryService;
import com.pezbackend.shared.interfaces.rest.resources.AuditEventResource;
import com.pezbackend.shared.interfaces.rest.transform.AuditEventResourceFromEntityAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Controlador REST que expone el historial de eventos de auditoría del sistema.
 * <p>
 * Este controlador expone un endpoint seguro y paginado para consultar los eventos históricos.
 * Está restringido a usuarios con el permiso granular audit.view.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditEventsController {

    private final AuditQueryService auditQueryService;
    private final com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository auditEventRepository;

    public AuditEventsController(
            AuditQueryService auditQueryService,
            com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository auditEventRepository
    ) {
        this.auditQueryService = auditQueryService;
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Registra un nuevo evento de auditoría en el sistema (por ejemplo, acciones de impresión en el cliente).
     */
    @PostMapping
    public ResponseEntity<?> createAuditEvent(@jakarta.validation.Valid @RequestBody com.pezbackend.shared.interfaces.rest.resources.CreateAuditEventResource resource) {
        String username = "system";
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            username = auth.getName();
        }

        AuditEvent auditEvent = new AuditEvent(
                resource.eventType(),
                resource.module(),
                username,
                resource.deviceId(),
                resource.payload() != null ? resource.payload() : java.util.Map.of(),
                resource.reason(),
                LocalDateTime.now()
        );

        AuditEvent saved = auditEventRepository.save(auditEvent);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(AuditEventResourceFromEntityAssembler.toResourceFromEntity(saved));
    }

    /**
     * Consulta y filtra el historial de auditoría de forma paginada.
     * <p>
     * Se puede filtrar opcionalmente por módulo, usuario, tipo de evento, y rango de fechas.
     * </p>
     *
     * @param module    nombre opcional del módulo de negocio a filtrar (ej. "orders")
     * @param userId    identificador opcional del usuario ejecutor de las acciones
     * @param from      fecha y hora inicial de inicio de rango de búsqueda (ISO date time)
     * @param to        fecha y hora final de fin de rango de búsqueda (ISO date time)
     * @param eventType nombre opcional del tipo de evento a filtrar (ej. "ItemCancelled")
     * @param pageable  parámetros de paginación y ordenamiento automáticos de Spring Data
     * @return página con la lista de eventos de auditoría mapeados a sus recursos DTO correspondientes
     */
    @RequiresPermission("audit.view")
    @GetMapping
    public ResponseEntity<Page<AuditEventResource>> getAuditEvents(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String eventType,
            Pageable pageable
    ) {
        Page<AuditEvent> eventsPage = auditQueryService.getAuditEvents(
                module,
                userId,
                eventType,
                from,
                to,
                pageable
        );

        Page<AuditEventResource> resourcesPage = eventsPage.map(
                AuditEventResourceFromEntityAssembler::toResourceFromEntity
        );

        return ResponseEntity.ok(resourcesPage);
    }
}
