package com.pezbackend.catalog.infrastructure.persistence.jpa.repositories;

import com.pezbackend.catalog.domain.model.entities.Recipe;
import com.pezbackend.catalog.domain.model.entities.RecipeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para acceder a los datos de las recetas de cocina.
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, RecipeId> {

    /**
     * Recupera todos los ingredientes de la receta de un producto específico.
     *
     * @param productId ID del producto
     * @return lista de ingredientes de la receta
     */
    List<Recipe> findAllByProductId(Long productId);

    /**
     * Busca un ingrediente específico en la receta de un producto.
     *
     * @param productId ID del producto
     * @param supplyId  ID del insumo
     * @return el ingrediente de la receta encontrado, si existe
     */
    Optional<Recipe> findByProductIdAndSupplyId(Long productId, Long supplyId);

    /**
     * Elimina un ingrediente específico de la receta de un producto.
     *
     * @param productId ID del producto
     * @param supplyId  ID del insumo
     */
    void deleteByProductIdAndSupplyId(Long productId, Long supplyId);
}
