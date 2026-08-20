package com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories;

import com.pezbackend.loyalty.domain.model.entities.MarketingNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad MarketingNotificationLog.
 */
@Repository
public interface MarketingNotificationLogRepository extends JpaRepository<MarketingNotificationLog, Long> {
    
    /**
     * Busca los logs de envío de promociones para un cliente después de una fecha/hora dada.
     *
     * @param customerId identificador del cliente
     * @param sentAt     fecha límite inicial
     * @return lista de logs encontrados
     */
    List<MarketingNotificationLog> findAllByCustomerIdAndSentAtAfter(Long customerId, LocalDateTime sentAt);
}
