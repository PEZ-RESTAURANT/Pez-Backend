package com.pezbackend.shared.infrastructure.persistence.jpa.repositories;

import com.pezbackend.shared.domain.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Interfaz de repositorio JPA para la persistencia y consulta de la entidad {@link AuditEvent}.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    /**
     * Recupera una página de registros de auditoría filtrados de acuerdo a varios criterios opcionales.
     * <p>
     * Si alguno de los parámetros de filtro es {@code null}, la consulta ignorará dicho filtro,
     * recuperando todos los elementos de ese subconjunto.
     * </p>
     *
     * @param module    nombre del módulo origen a filtrar (opcional)
     * @param userId    identificador del usuario a filtrar (opcional)
     * @param eventType nombre del evento a filtrar (opcional)
     * @param fromDate  límite de fecha y hora inicial (opcional, inclusivo)
     * @param toDate    límite de fecha y hora final (opcional, inclusivo)
     * @param pageable  parámetros de paginación de Spring Data
     * @return página con los eventos de auditoría que cumplen las condiciones del filtro
     */
    @Query("""
        SELECT a FROM AuditEvent a
        WHERE (:module IS NULL OR a.module = :module)
          AND (:userId IS NULL OR a.userId = :userId)
          AND (:eventType IS NULL OR a.eventType = :eventType)
          AND (:fromDate IS NULL OR a.timestamp >= :fromDate)
          AND (:toDate IS NULL OR a.timestamp <= :toDate)
        """)
    Page<AuditEvent> findByFilters(
            @Param("module") String module,
            @Param("userId") String userId,
            @Param("eventType") String eventType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}
