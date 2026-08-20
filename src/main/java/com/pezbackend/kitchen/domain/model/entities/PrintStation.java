package com.pezbackend.kitchen.domain.model.entities;

import org.hibernate.annotations.Filter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Entidad JPA que representa una estación/puesto de trabajo de impresión (ej. Caja 1, Barra).
 */
@Entity
@Table(name = "print_stations")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class PrintStation extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    protected PrintStation() {}

    public PrintStation(String name) {
        this.name = name;
    }
}
