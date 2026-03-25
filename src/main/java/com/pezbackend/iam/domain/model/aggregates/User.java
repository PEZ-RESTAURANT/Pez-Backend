package com.pezbackend.iam.domain.model.aggregates;

import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
public class User extends AuditableAbstractAggregateRoot<User> {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;
    protected User() {
        this.roles = new ArrayList<>();
    }

    /**
     * Constructor para el agregado User
     * Solo contiene información de autenticación y autorización (IAM)
     * @param email el correo electrónico
     * @param passwordHash el hash de la contraseña
     * @param firstName el nombre
     * @param lastName el apellido
     * @param isVerified si está verificado
     */
    public User(String email, String passwordHash, String firstName, String lastName, boolean isVerified) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isVerified = isVerified;
        this.roles = new ArrayList<>();
    }
    
    public void addRole(Role role) {
        if (role != null && !this.roles.contains(role)) {
            this.roles.add(role);
        }
    }
    
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }
}
