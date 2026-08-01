package com.pezbackend.catalog.interfaces.rest.transform;

import com.pezbackend.catalog.domain.model.entities.Recipe;
import com.pezbackend.catalog.interfaces.rest.resources.RecipeResource;

/**
 * Ensamblador para transformar entidades Recipe a recursos RecipeResource DTO.
 */
public class RecipeResourceFromEntityAssembler {

    /**
     * Convierte una entidad Recipe en un recurso RecipeResource.
     *
     * @param entity la entidad Recipe
     * @return el recurso DTO correspondiente
     */
    public static RecipeResource toResourceFromEntity(Recipe entity) {
        return new RecipeResource(
                entity.getProductId(),
                entity.getSupplyId(),
                entity.getQuantityUsed()
        );
    }
}
