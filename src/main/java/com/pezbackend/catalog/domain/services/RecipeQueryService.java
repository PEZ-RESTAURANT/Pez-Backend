package com.pezbackend.catalog.domain.services;

import com.pezbackend.catalog.domain.model.entities.Recipe;

import java.util.List;

/**
 * Servicio de consultas para obtener las recetas de productos en el catálogo.
 */
public interface RecipeQueryService {

    /**
     * Obtiene los ingredientes que componen la receta de un producto específico.
     *
     * @param productId ID del producto
     * @return lista de ingredientes de la receta
     */
    List<Recipe> getRecipeForProduct(Long productId);
}
