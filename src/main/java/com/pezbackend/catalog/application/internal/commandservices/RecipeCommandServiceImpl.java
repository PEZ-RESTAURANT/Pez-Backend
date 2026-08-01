package com.pezbackend.catalog.application.internal.commandservices;

import com.pezbackend.catalog.domain.model.entities.Recipe;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.RecipeRepository;
import com.pezbackend.catalog.domain.services.RecipeCommandService;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del servicio de comandos para la gestión de recetas de productos.
 */
@Service
@RequiredArgsConstructor
public class RecipeCommandServiceImpl implements RecipeCommandService {

    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;
    private final SupplyRepository supplyRepository;

    @Override
    @Transactional
    public void addOrUpdateRecipeItem(Long productId, Long supplyId, BigDecimal quantityUsed) {
        // 1. Validar que el producto exista
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Producto no encontrado con ID: " + productId);
        }

        // 2. Validar que el insumo exista en el almacén
        if (!supplyRepository.existsById(supplyId)) {
            throw new ResourceNotFoundException("SUPPLY_NOT_FOUND", "Insumo no encontrado con ID: " + supplyId);
        }

        // 3. Validar cantidad mayor a cero
        if (quantityUsed == null || quantityUsed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("INVALID_QUANTITY", "La cantidad del ingrediente debe ser mayor a cero.");
        }

        // 4. Crear o actualizar ingrediente
        Recipe item = recipeRepository.findByProductIdAndSupplyId(productId, supplyId)
                .orElseGet(() -> new Recipe(productId, supplyId, quantityUsed));
        
        item.setQuantityUsed(quantityUsed);
        recipeRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteRecipeItem(Long productId, Long supplyId) {
        if (!recipeRepository.findByProductIdAndSupplyId(productId, supplyId).isPresent()) {
            throw new ResourceNotFoundException("RECIPE_ITEM_NOT_FOUND", "Insumo no encontrado en la receta del producto.");
        }
        recipeRepository.deleteByProductIdAndSupplyId(productId, supplyId);
    }
}
