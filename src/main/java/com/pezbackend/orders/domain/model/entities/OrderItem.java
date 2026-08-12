package com.pezbackend.orders.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.orders.domain.model.valueobjects.OrderItemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pezbackend.orders.domain.model.aggregates.Order;

import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Representa un plato o ítem individual comandado dentro de un pedido.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class OrderItem extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public Long getOrderId() {
        return order != null ? order.getId() : null;
    }

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 255)
    private String note;

    @Column(name = "waiter_id", nullable = false)
    private Long waiterId;

    @Column(name = "unit_price_snapshot", nullable = false)
    private BigDecimal unitPriceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderItemStatus status = OrderItemStatus.PENDING;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected OrderItem() {}

    /**
     * Construye un nuevo ítem de comanda en estado inicial PENDING.
     *
     * @param productId          ID del producto del catálogo
     * @param quantity           cantidad solicitada
     * @param note               notas o personalizaciones del plato (ej: "sin cebolla")
     * @param waiterId           ID del mozo que comanda la acción (mesa compartida)
     * @param unitPriceSnapshot  precio unitario del producto al momento de comandar
     */
    public OrderItem(Long productId, Integer quantity, String note, Long waiterId, BigDecimal unitPriceSnapshot) {
        this.productId = productId;
        this.quantity = quantity;
        this.note = note;
        this.waiterId = waiterId;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.status = OrderItemStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}