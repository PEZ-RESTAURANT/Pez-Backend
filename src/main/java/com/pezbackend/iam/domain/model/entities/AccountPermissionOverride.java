package com.pezbackend.iam.domain.model.entities;

import com.pezbackend.iam.domain.model.valueobjects.OverrideValue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una excepción o anulación individual de permiso asignada a un usuario específico.
 * <p>
 * Los overrides tienen prioridad sobre las configuraciones por defecto basadas en roles.
 * </p>
 */
@Entity
@Table(name = "account_permission_overrides", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "permission_id"})
})
@Getter
@Setter
public class AccountPermissionOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "override_value", nullable = false, length = 20)
    private OverrideValue value;

    @Column(name = "granted_by", nullable = false, length = 100)
    private String grantedBy;

    @Column(name = "override_date", nullable = false)
    private LocalDateTime date;

    @Column(name = "reason", length = 255)
    private String reason;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected AccountPermissionOverride() {}

    /**
     * Construye un override individual de permiso para un usuario.
     *
     * @param userId     el identificador único del usuario afectado por la anulación
     * @param permission el permiso granular objeto de la anulación
     * @param value      el valor de la anulación (concedido o revocado)
     * @param grantedBy  el identificador (userId / email) del administrador ejecutor
     * @param reason     justificación u observaciones sobre la anulación (opcional)
     */
    public AccountPermissionOverride(Long userId, Permission permission, OverrideValue value, String grantedBy, String reason) {
        this.userId = userId;
        this.permission = permission;
        this.value = value;
        this.grantedBy = grantedBy;
        this.date = LocalDateTime.now();
        this.reason = reason;
    }
}
