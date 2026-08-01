package com.pezbackend.iam.domain.model.entities;

import com.pezbackend.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa la configuración de concesión por defecto de un permiso del catálogo para un rol específico.
 */
@Entity
@Table(name = "role_permission_defaults", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_name", "permission_id"})
})
@Getter
@Setter
public class RolePermissionDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 50)
    private Roles role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(nullable = false)
    private boolean granted;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected RolePermissionDefault() {}

    /**
     * Construye una configuración por defecto para un rol y permiso.
     *
     * @param role       el rol de seguridad de IAM al que aplica la regla
     * @param permission el permiso granular del catálogo
     * @param granted    indica si el permiso se concede por defecto (true) o no (false)
     */
    public RolePermissionDefault(Roles role, Permission permission, boolean granted) {
        this.role = role;
        this.permission = permission;
        this.granted = granted;
    }
}
