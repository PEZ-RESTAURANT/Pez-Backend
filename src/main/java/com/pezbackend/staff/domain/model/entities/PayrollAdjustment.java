package com.pezbackend.staff.domain.model.entities;

import com.pezbackend.staff.domain.model.valueobjects.PayrollAdjustmentType;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA que representa un ajuste de nómina (adelanto de sueldo o deducción por consumo).
 */
@Entity
@Table(name = "payroll_adjustments")
@Getter
@Setter
public class PayrollAdjustment extends AuditableModel {

    @NotNull
    @Column(nullable = false)
    private Long staffProfileId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PayrollAdjustmentType type;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column
    private Long saleId;

    @NotNull
    @Column(nullable = false, length = 255)
    private String registeredBy;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected PayrollAdjustment() {}

    /**
     * Construye un nuevo ajuste de nómina.
     *
     * @param staffProfileId id del perfil del empleado
     * @param type           tipo de ajuste (ADVANCE | CONSUMPTION_DEDUCTION)
     * @param amount         monto del ajuste
     * @param saleId         id de la venta ligada (opcional, solo para CONSUMPTION_DEDUCTION)
     * @param registeredBy   identificador del usuario administrador o cajero que registra
     * @param date           fecha de aplicación del ajuste
     */
    public PayrollAdjustment(Long staffProfileId, PayrollAdjustmentType type, BigDecimal amount, Long saleId, String registeredBy, LocalDate date) {
        this.staffProfileId = staffProfileId;
        this.type = type;
        this.amount = amount;
        this.saleId = saleId;
        this.registeredBy = registeredBy;
        this.date = date;
    }
}
