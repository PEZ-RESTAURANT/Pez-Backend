package com.pezbackend.catalog.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para recibir la entrada de datos al agregar/modificar un ingrediente en la receta de un producto.
 *
 * @param supplyId     ID del insumo
 * @param quantityUsed cantidad de insumo utilizada
 */
public record AddRecipeItemResource(
        Long supplyId,
        BigDecimal quantityUsed
) {}
