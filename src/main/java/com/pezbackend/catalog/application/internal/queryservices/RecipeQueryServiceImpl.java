package com.pezbackend.catalog.application.internal.queryservices;

import com.pezbackend.catalog.domain.model.entities.Recipe;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.RecipeRepository;
import com.pezbackend.catalog.domain.services.RecipeQueryService;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de consultas para recetas de productos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeQueryServiceImpl implements RecipeQueryService {

    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;

    @Override
    public List<Recipe> getRecipeForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Producto no encontrado con ID: " + productId);
        }
        return recipeRepository.findAllByProductId(productId);
    }
}
