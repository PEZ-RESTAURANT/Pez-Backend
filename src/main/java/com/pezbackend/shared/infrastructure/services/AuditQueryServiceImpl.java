package com.pezbackend.shared.infrastructure.services;

import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.domain.services.AuditQueryService;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación concreta de {@link AuditQueryService}.
 * <p>
 * Maneja la lógica de normalización de filtros de entrada y delega la ejecución de la consulta
 * paginada directamente en el repositorio JPA de auditoría.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuditQueryServiceImpl implements AuditQueryService {

    private final AuditEventRepository auditEventRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEvents(
            String module,
            String userId,
            String eventType,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        // Normalizar strings vacíos o compuestos únicamente de espacios en blanco a null
        String normalizedModule = (module != null && !module.isBlank()) ? module.trim() : null;
        String normalizedUserId = (userId != null && !userId.isBlank()) ? userId.trim() : null;
        String normalizedEventType = (eventType != null && !eventType.isBlank()) ? eventType.trim() : null;

        return auditEventRepository.findByFilters(
                normalizedModule,
                normalizedUserId,
                normalizedEventType,
                from,
                to,
                pageable
        );
    }
}
