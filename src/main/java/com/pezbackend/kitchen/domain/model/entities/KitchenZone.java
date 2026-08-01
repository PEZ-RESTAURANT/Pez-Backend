package com.pezbackend.kitchen.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad JPA que representa una zona de cocina (ej: Bar, Cocina Caliente, Cocina Fría).
 */
@Entity
@Table(name = "kitchen_zones")
@Getter
@Setter
public class KitchenZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected KitchenZone() {}

    /**
     * Construye una nueva zona de cocina.
     *
     * @param name nombre descriptivo de la zona
     */
    public KitchenZone(String name) {
        this.name = name;
    }
}
