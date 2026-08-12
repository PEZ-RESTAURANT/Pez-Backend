package com.pezbackend.staff.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.staff.domain.model.valueobjects.AttendanceMethod;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;

/**
 * Entidad JPA que representa un registro de marca de asistencia (entrada y salida) de un empleado.
 */
@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class AttendanceRecord extends AbstractTenantEntity {

    @NotNull
    @Column(nullable = false)
    private Long staffProfileId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime checkInAt;

    @Column
    private LocalDateTime checkOutAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttendanceMethod method;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected AttendanceRecord() {}

    /**
     * Construye un registro de entrada de asistencia.
     *
     * @param staffProfileId id del perfil del empleado
     * @param checkInAt      fecha y hora de entrada
     * @param method         método de entrada
     */
    public AttendanceRecord(Long staffProfileId, LocalDateTime checkInAt, AttendanceMethod method) {
        this.staffProfileId = staffProfileId;
        this.checkInAt = checkInAt;
        this.method = method;
    }

    /**
     * Registra la salida de la asistencia.
     *
     * @param checkOutAt fecha y hora de salida
     */
    public void recordCheckOut(LocalDateTime checkOutAt) {
        this.checkOutAt = checkOutAt;
    }
}