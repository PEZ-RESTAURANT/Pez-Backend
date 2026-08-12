package com.pezbackend.shared.domain.model;

import org.hibernate.annotations.Filter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Entidad de persistencia que almacena un registro histórico de eventos de auditoría del sistema.
 * <p>
 * Representa la tabla de base de datos {@code audit_events} y hace uso de las capacidades
 * nativas de JSON de Hibernate 6 para almacenar la carga útil dinámica del evento.
 * </p>
 */
@Entity
@Table(name = "audit_events")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class AuditEvent extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "module", nullable = false, length = 100)
    private String module;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "json", nullable = false)
    private Map<String, Object> payload;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected AuditEvent() {}

    /**
     * Construye un nuevo registro de auditoría con la información del evento de dominio correspondiente.
     *
     * @param eventType tipo de evento ocurrido
     * @param module    módulo de negocio que emitió el evento
     * @param userId    usuario que ejecutó la acción
     * @param deviceId  dispositivo desde donde se originó la acción (puede ser nulo)
     * @param payload   información específica detallada en formato clave-valor
     * @param reason    justificación dada para la acción (puede ser nulo)
     * @param timestamp marca de tiempo en la que se produjo el evento
     */
    public AuditEvent(String eventType, String module, String userId, String deviceId, Map<String, Object> payload, String reason, LocalDateTime timestamp) {
        this.eventType = eventType;
        this.module = module;
        this.userId = userId;
        this.deviceId = deviceId;
        this.payload = payload;
        this.reason = reason;
        this.timestamp = timestamp;
    }
}