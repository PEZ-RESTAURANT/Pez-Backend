package com.pezbackend.catalog.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO que representa un ingrediente individual en la receta de un producto.
 *
 * @param productId    ID del producto
 * @param supplyId     ID del insumo
 * @param supplyName   Nombre del insumo
 * @param supplyUnit   Unidad de medida del insumo
 * @param quantityUsed cantidad de insumo utilizada
 */
public record RecipeResource(
        Long productId,
        Long supplyId,
        String supplyName,
        String supplyUnit,
        BigDecimal quantityUsed
) {}
