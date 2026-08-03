package com.pezbackend.inventory.domain.model.entities;

import com.pezbackend.inventory.domain.model.valueobjects.StockMovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Entidad JPA que representa una transacción de movimiento en el stock de un insumo.
 */
@Entity
@Table(name = "stock_movements")
@Getter
@Setter
public class StockMovement extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supply_id", nullable = false)
    private Long supplyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StockMovementType type;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(name = "registered_by", nullable = false, length = 100)
    private String registeredBy;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 255)
    private String reason;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected StockMovement() {}

    /**
     * Construye una transacción de movimiento de stock.
     *
     * @param supplyId     ID del insumo
     * @param type         tipo de movimiento (RESTOCK, SALE_DEDUCTION, MANUAL_ADJUSTMENT)
     * @param quantity     cantidad del movimiento (valor absoluto o firmado, de acuerdo al diseño)
     * @param registeredBy usuario ejecutor
     * @param reason       justificación o motivo
     */
    public StockMovement(Long supplyId, StockMovementType type, BigDecimal quantity, String registeredBy, String reason) {
        this.supplyId = supplyId;
        this.type = type;
        this.quantity = quantity;
        this.registeredBy = registeredBy;
        this.reason = reason;
        this.date = LocalDateTime.now();
    }
}
