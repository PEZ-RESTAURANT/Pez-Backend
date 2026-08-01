package com.pezbackend.catalog.domain.services;

import java.math.BigDecimal;

/**
 * Servicio de comandos para gestionar los insumos que componen la receta de un producto.
 */
public interface RecipeCommandService {

    /**
     * Agrega un insumo a la receta de un producto, o actualiza su cantidad si ya existía.
     *
     * @param productId    ID del producto
     * @param supplyId     ID del insumo
     * @param quantityUsed cantidad utilizada
     */
    void addOrUpdateRecipeItem(Long productId, Long supplyId, BigDecimal quantityUsed);

    /**
     * Elimina un insumo de la receta de un producto.
     *
     * @param productId ID del producto
     * @param supplyId  ID del insumo
     */
    void deleteRecipeItem(Long productId, Long supplyId);
}
