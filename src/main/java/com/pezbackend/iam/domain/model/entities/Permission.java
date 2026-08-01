package com.pezbackend.iam.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa un permiso granular dentro del catálogo del sistema.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String module;

    @Column(nullable = false, length = 255)
    private String description;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Permission() {}

    /**
     * Construye un nuevo permiso para el catálogo del sistema.
     *
     * @param code        el código identificador único y estable del permiso (ej. "orders.cancel_item")
     * @param module      el módulo al que pertenece el permiso (ej. "orders")
     * @param description la descripción descriptiva del propósito del permiso
     */
    public Permission(String code, String module, String description) {
        this.code = code;
        this.module = module;
        this.description = description;
    }
}
