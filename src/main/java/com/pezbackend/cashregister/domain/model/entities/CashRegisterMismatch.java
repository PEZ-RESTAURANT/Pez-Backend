package com.pezbackend.cashregister.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que registra descuadres al momento del arqueo/cierre de una caja registradora.
 */
@Entity
@Table(name = "cash_register_mismatches")
@Getter
@Setter
public class CashRegisterMismatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cash_register_id", nullable = false)
    private Long cashRegisterId;

    @Column(name = "expected_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal expectedAmount;

    @Column(name = "declared_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal declaredAmount;

    @Column(nullable = false, length = 50)
    private String status; // MATCHED, MISMATCHED

    @Column(name = "notified_admin_at")
    private LocalDateTime notifiedAdminAt;

    protected CashRegisterMismatch() {}

    public CashRegisterMismatch(Long cashRegisterId, BigDecimal expectedAmount, BigDecimal declaredAmount, String status, LocalDateTime notifiedAdminAt) {
        this.cashRegisterId = cashRegisterId;
        this.expectedAmount = expectedAmount;
        this.declaredAmount = declaredAmount;
        this.status = status;
        this.notifiedAdminAt = notifiedAdminAt;
    }
}
