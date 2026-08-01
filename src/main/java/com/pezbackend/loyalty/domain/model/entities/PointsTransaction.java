package com.pezbackend.loyalty.domain.model.entities;

import com.pezbackend.loyalty.domain.model.valueobjects.PointsTransactionType;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entidad JPA que representa una transacción histórica de acumulación o canje de puntos.
 */
@Entity
@Table(name = "points_transactions")
@Getter
@Setter
public class PointsTransaction extends AuditableModel {

    @NotNull
    @Column(nullable = false)
    private Long customerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PointsTransactionType type;

    @NotNull
    @Column(nullable = false)
    private int amount;

    @Column
    private Long saleId;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected PointsTransaction() {}

    /**
     * Construye una nueva transacción de puntos.
     *
     * @param customerId id del cliente
     * @param type       tipo de transacción (EARNED | REDEEMED)
     * @param amount     cantidad de puntos
     * @param saleId     id de la venta (opcional, solo para EARNED)
     * @param date       fecha de la transacción
     */
    public PointsTransaction(Long customerId, PointsTransactionType type, int amount, Long saleId, LocalDate date) {
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.saleId = saleId;
        this.date = date;
    }
}
