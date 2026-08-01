package com.pezbackend.staff.domain.model.entities;

import com.pezbackend.staff.domain.model.valueobjects.SanctionType;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entidad JPA que representa una sanción aplicada a un empleado.
 */
@Entity
@Table(name = "sanctions")
@Getter
@Setter
public class Sanction extends AuditableModel {

    @NotNull
    @Column(nullable = false)
    private Long staffProfileId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SanctionType type;

    @NotNull
    @Column(nullable = false, length = 2000)
    private String reason;

    @NotNull
    @Column(nullable = false, length = 255)
    private String registeredBy;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Sanction() {}

    /**
     * Construye un registro de sanción.
     *
     * @param staffProfileId id del perfil del empleado
     * @param type           tipo de sanción
     * @param reason         explicación del motivo
     * @param registeredBy   identificador del usuario que registra la sanción
     * @param date           fecha de la sanción
     */
    public Sanction(Long staffProfileId, SanctionType type, String reason, String registeredBy, LocalDate date) {
        this.staffProfileId = staffProfileId;
        this.type = type;
        this.reason = reason;
        this.registeredBy = registeredBy;
        this.date = date;
    }
}
