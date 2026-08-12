package com.pezbackend.orders.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.orders.domain.model.valueobjects.PriceAdjustmentScope;
import com.pezbackend.orders.domain.model.valueobjects.PriceAdjustmentValidity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pezbackend.orders.domain.model.aggregates.Order;

import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Representa un ajuste manual de precio o descuento aplicado sobre un pedido por un administrador.
 */
@Entity
@Table(name = "price_adjustments")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class PriceAdjustment extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public Long getOrderId() {
        return order != null ? order.getId() : null;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PriceAdjustmentScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PriceAdjustmentValidity validity;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "new_value", nullable = false)
    private BigDecimal newValue;

    @Column(name = "applied_by", nullable = false, length = 100)
    private String appliedBy;

    @Column(nullable = false, length = 255)
    private String reason;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected PriceAdjustment() {}

    /**
     * Construye una nueva entidad de ajuste de precio.
     *
     * @param scope     alcance del ajuste (INDIVIDUAL, GROUP, ALL)
     * @param validity  validez temporal (PERMANENT, TEMPORARY)
     * @param startAt   inicio de validez (solo para TEMPORARY)
     * @param endAt     fin de validez (solo para TEMPORARY)
     * @param newValue  nuevo valor o tarifa a aplicar
     * @param appliedBy administrador ejecutor del ajuste (username/email)
     * @param reason    justificación del ajuste
     */
    public PriceAdjustment(PriceAdjustmentScope scope, PriceAdjustmentValidity validity,
                           LocalDateTime startAt, LocalDateTime endAt, BigDecimal newValue,
                           String appliedBy, String reason) {
        this.scope = scope;
        this.validity = validity;
        this.startAt = startAt;
        this.endAt = endAt;
        this.newValue = newValue;
        this.appliedBy = appliedBy;
        this.reason = reason;
    }
}