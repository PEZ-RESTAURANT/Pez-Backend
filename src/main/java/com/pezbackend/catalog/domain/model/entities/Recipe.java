package com.pezbackend.catalog.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa una asociación de receta, indicando qué insumo y en qué cantidad usa un producto.
 * Mapeado por una clave compuesta compuesta por productId y supplyId.
 */
@Entity
@Table(name = "recipes")
@IdClass(RecipeId.class)
@Getter
@Setter
public class Recipe {

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Id
    @Column(name = "supply_id", nullable = false)
    private Long supplyId;

    @Column(name = "quantity_used", nullable = false, precision = 10, scale = 4)
    private BigDecimal quantityUsed;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Recipe() {}

    /**
     * Construye un nuevo ingrediente de receta.
     *
     * @param productId    ID del producto
     * @param supplyId     ID del insumo
     * @param quantityUsed cantidad de insumo utilizada por el producto
     */
    public Recipe(Long productId, Long supplyId, BigDecimal quantityUsed) {
        this.productId = productId;
        this.supplyId = supplyId;
        this.quantityUsed = quantityUsed;
    }
}
