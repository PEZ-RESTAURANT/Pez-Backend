package com.pezbackend.loyalty.domain.model.entities;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

/**
 * Entidad JPA multi-tenant que audita y registra el envío de correos promocionales de marketing.
 */
@Entity
@Table(name = "marketing_notification_logs")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class MarketingNotificationLog extends AbstractTenantEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType; // "PROMO"

    protected MarketingNotificationLog() {}

    public MarketingNotificationLog(Long customerId, String notificationType) {
        this.customerId = customerId;
        this.sentAt = LocalDateTime.now();
        this.notificationType = notificationType;
    }
}
