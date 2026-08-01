package com.pezbackend.shared.domain.services;

import com.pezbackend.shared.domain.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Interface del servicio de aplicación que define el caso de uso para consultar
 * el historial de eventos de auditoría registrados en la aplicación.
 */
public interface AuditQueryService {

    /**
     * Recupera una página de registros de auditoría que cumplen con los filtros especificados.
     *
     * @param module    nombre del módulo opcional para filtrar los eventos
     * @param userId    identificador único opcional del usuario que generó los eventos
     * @param eventType nombre de tipo de evento opcional para filtrar los eventos
     * @param from      fecha y hora inicial del rango de búsqueda (opcional, inclusivo)
     * @param to        fecha y hora final del rango de búsqueda (opcional, inclusivo)
     * @param pageable  parámetros de paginación y ordenamiento
     * @return página conteniendo los {@link AuditEvent} que correspondan a la búsqueda
     */
    Page<AuditEvent> getAuditEvents(
            String module,
            String userId,
            String eventType,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}
