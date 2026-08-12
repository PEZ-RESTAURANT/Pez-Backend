package com.pezbackend.staff.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;

/**
 * Entidad JPA que representa un registro de horas extras laboradas por un empleado.
 */
@Entity
@Table(name = "overtime_records")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class OvertimeRecord extends AbstractTenantEntity {

    @NotNull
    @Column(nullable = false)
    private Long staffProfileId;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @Column(nullable = false, length = 255)
    private String registeredBy;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected OvertimeRecord() {}

    /**
     * Construye un nuevo registro de horas extras.
     *
     * @param staffProfileId id del perfil del empleado
     * @param hours          cantidad de horas extras
     * @param date           fecha en que se realizaron las horas extras
     * @param registeredBy   identificador del usuario que registra
     */
    public OvertimeRecord(Long staffProfileId, BigDecimal hours, LocalDate date, String registeredBy) {
        this.staffProfileId = staffProfileId;
        this.hours = hours;
        this.date = date;
        this.registeredBy = registeredBy;
    }
}