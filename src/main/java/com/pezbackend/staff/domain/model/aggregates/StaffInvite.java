package com.pezbackend.staff.domain.model.aggregates;

import com.pezbackend.shared.domain.model.aggregates.AbstractTenantAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

/**
 * Representa una invitación generada por un administrador para que un empleado
 * cree su cuenta en el sistema con un rol y restaurante predefinido.
 */
@Entity
@Table(name = "staff_invites")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class StaffInvite extends AbstractTenantAggregateRoot<StaffInvite> {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String requestedRole;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Constructor requerido por JPA.
     */
    protected StaffInvite() {}

    /**
     * Construye una nueva invitación.
     *
     * @param code          código corto único de la invitación
     * @param requestedRole rol asignado al nuevo empleado
     * @param email         correo del empleado invitado
     * @param expiresAt     fecha y hora límite para aceptar la invitación
     */
    public StaffInvite(String code, String requestedRole, String email, LocalDateTime expiresAt) {
        this.code = code;
        this.requestedRole = requestedRole;
        this.email = email;
        this.expiresAt = expiresAt;
        this.used = false;
        this.revoked = false;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isValid() {
        return !used && !revoked && !isExpired();
    }
}
