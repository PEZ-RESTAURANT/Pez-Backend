package com.pezbackend.orders.domain.model.aggregates;

import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.PriceAdjustment;
import com.pezbackend.orders.domain.model.valueobjects.OrderStatus;
import com.pezbackend.orders.domain.model.valueobjects.OrderType;
import com.pezbackend.shared.domain.exceptions.InvalidStateTransitionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Agregado raíz (Aggregate Root) que representa un pedido o comanda en el sistema.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private Long tableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderType type;

    @Column(name = "customer_id")
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.FREE;

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PriceAdjustment> priceAdjustments = new ArrayList<>();

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Order() {}

    /**
     * Construye un nuevo pedido.
     *
     * @param tableId    ID de la mesa asociada (null si es TAKEAWAY o DELIVERY)
     * @param type       tipo de pedido (DINE_IN, TAKEAWAY, DELIVERY)
     * @param customerId ID del cliente (opcional)
     */
    public Order(Long tableId, OrderType type, Long customerId) {
        this.tableId = tableId;
        this.type = type;
        this.customerId = customerId;
        this.createdAt = LocalDateTime.now();
        
        if (type == OrderType.DINE_IN) {
            this.status = OrderStatus.FREE;
        } else {
            this.status = OrderStatus.TAKING_ORDER;
        }
    }

    /**
     * Transiciona el estado del pedido validando las reglas de negocio de la máquina de estados.
     *
     * @param newStatus nuevo estado de destino
     * @throws InvalidStateTransitionException si la transición no es válida en la máquina de estados
     */
    public void transitionTo(OrderStatus newStatus) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new InvalidStateTransitionException(
                    "INVALID_STATE_TRANSITION",
                    "No se permite transicionar el pedido del estado " + this.status + " al " + newStatus
            );
        }
        this.status = newStatus;
        if (newStatus == OrderStatus.TAKING_ORDER && this.attendedAt == null) {
            this.attendedAt = LocalDateTime.now();
        }
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return true;
        }
        
        return switch (current) {
            case FREE -> next == OrderStatus.UNATTENDED || next == OrderStatus.TAKING_ORDER;
            case UNATTENDED -> next == OrderStatus.TAKING_ORDER;
            case TAKING_ORDER -> next == OrderStatus.WAITING_DISHES;
            case WAITING_DISHES -> next == OrderStatus.ALL_DELIVERED;
            case ALL_DELIVERED -> next == OrderStatus.ISSUED_UNPAID || next == OrderStatus.WAITING_DISHES;
            case ISSUED_UNPAID -> next == OrderStatus.PAID;
            case PAID -> next == OrderStatus.FREE;
        };
    }

    /**
     * Añade un ítem (plato) a la comanda.
     *
     * @param item plato a comandar
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

    /**
     * Añade un ajuste manual de precio a la comanda.
     *
     * @param adjustment ajuste de precio
     */
    public void addPriceAdjustment(PriceAdjustment adjustment) {
        this.priceAdjustments.add(adjustment);
        adjustment.setOrder(this);
    }
}
