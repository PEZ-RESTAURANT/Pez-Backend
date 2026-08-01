package com.pezbackend.shared.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Recurso DTO (Data Transfer Object) utilizado para exponer los datos de un evento
 * de auditoría en las respuestas HTTP de la API REST.
 *
 * @param id        identificador único autogenerado de la auditoría
 * @param eventType tipo de evento ocurrido (ej. "ItemCancelled")
 * @param module    módulo de negocio que emitió el evento (ej. "orders")
 * @param userId    usuario que ejecutó la acción
 * @param deviceId  dispositivo desde donde se originó la acción (puede ser nulo)
 * @param payload   carga útil dinámica con datos específicos del evento en formato clave-valor
 * @param reason    motivo justificado de la acción (puede ser nulo)
 * @param timestamp fecha y hora precisa en la que ocurrió el evento de dominio
 */
public record AuditEventResource(
        Long id,
        String eventType,
        String module,
        String userId,
        String deviceId,
        Map<String, Object> payload,
        String reason,
        LocalDateTime timestamp
) {}
