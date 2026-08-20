package com.pezbackend.tenancy.domain.model.aggregates;

import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa la entidad Restaurant, raíz del sistema multi-tenant.
 */
@Entity
@Table(name = "restaurants")
@Getter
@Setter
public class Restaurant extends AuditableModel {

    @NotNull
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String businessDocumentNumber;

    @Column(length = 255)
    private String contactEmail;

    @Column(length = 50)
    private String contactPhone;

    @Column(length = 255)
    private String address;

    @NotNull
    @Column(nullable = false)
    private Boolean active = true;

    protected Restaurant() {}

    public Restaurant(String name, String businessDocumentNumber, String contactEmail, String contactPhone) {
        this.name = name;
        this.businessDocumentNumber = businessDocumentNumber;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.active = true;
    }

    public Restaurant(String name, String businessDocumentNumber, String contactEmail, String contactPhone, String address) {
        this(name, businessDocumentNumber, contactEmail, contactPhone);
        this.address = address;
    }
}
