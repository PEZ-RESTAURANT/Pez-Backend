package com.pezbackend.catalog.interfaces.rest;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.catalog.domain.model.commands.CreateProductCommand;
import com.pezbackend.catalog.domain.model.commands.DeleteProductCommand;
import com.pezbackend.catalog.domain.model.commands.UpdateProductCommand;
import com.pezbackend.catalog.domain.model.queries.*;
import com.pezbackend.catalog.domain.services.ProductCommandService;
import com.pezbackend.catalog.domain.services.ProductQueryService;
import com.pezbackend.catalog.interfaces.rest.resources.CreateProductResource;
import com.pezbackend.catalog.interfaces.rest.resources.ProductResource;
import com.pezbackend.catalog.interfaces.rest.resources.UpdateProductResource;
import com.pezbackend.catalog.interfaces.rest.transform.CreateProductCommandFromResourceAssembler;
import com.pezbackend.catalog.interfaces.rest.transform.ProductResourceFromEntityAssembler;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.AuthorizeRoles;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;
import com.pezbackend.shared.domain.exceptions.TenantMismatchException;
import com.pezbackend.shared.infrastructure.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.pezbackend.catalog.domain.services.ProductKitchenZoneCommandService;
import com.pezbackend.catalog.interfaces.rest.resources.AssignProductToZoneResource;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;

import com.pezbackend.catalog.domain.services.RecipeCommandService;
import com.pezbackend.catalog.domain.services.RecipeQueryService;
import com.pezbackend.catalog.domain.model.entities.Recipe;
import com.pezbackend.catalog.interfaces.rest.resources.AddRecipeItemResource;
import com.pezbackend.catalog.interfaces.rest.resources.RecipeResource;
import com.pezbackend.catalog.interfaces.rest.transform.RecipeResourceFromEntityAssembler;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.catalog.domain.services.ProductKitchenZoneQueryService;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.inventory.domain.model.entities.Supply;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductCommandService commandService;
    private final ProductQueryService queryService;
    private final ProductKitchenZoneCommandService productKitchenZoneCommandService;
    private final ProductKitchenZoneQueryService productKitchenZoneQueryService;
    private final RecipeCommandService recipeCommandService;
    private final RecipeQueryService recipeQueryService;
    private final CategoryRepository categoryRepository;
    private final SupplyRepository supplyRepository;

    public ProductController(ProductCommandService commandService,
                             ProductQueryService queryService,
                             ProductKitchenZoneCommandService productKitchenZoneCommandService,
                             ProductKitchenZoneQueryService productKitchenZoneQueryService,
                             RecipeCommandService recipeCommandService,
                             RecipeQueryService recipeQueryService,
                             CategoryRepository categoryRepository,
                             SupplyRepository supplyRepository) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.productKitchenZoneCommandService = productKitchenZoneCommandService;
        this.productKitchenZoneQueryService = productKitchenZoneQueryService;
        this.recipeCommandService = recipeCommandService;
        this.recipeQueryService = recipeQueryService;
        this.categoryRepository = categoryRepository;
        this.supplyRepository = supplyRepository;
    }

    private void validateCategoryTenant(Category category) {
        Long currentTenant = TenantContext.getCurrentTenantId();
        if (currentTenant != null && !currentTenant.equals(category.getRestaurantId())) {
            throw new TenantMismatchException("Category", category.getId());
        }
    }

    private void validateProductTenant(Product product) {
        Long currentTenant = TenantContext.getCurrentTenantId();
        if (currentTenant != null && !currentTenant.equals(product.getRestaurantId())) {
            throw new TenantMismatchException("Product", product.getId());
        }
    }

    // 🔥 CREATE
    @RequiresPermission("catalog.edit_products_categories")
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateProductResource resource) {
        Category category = categoryRepository.findById(resource.categoryId())
                .orElseThrow(() -> new TenantMismatchException("Category", resource.categoryId()));
        validateCategoryTenant(category);

        CreateProductCommand command =
                CreateProductCommandFromResourceAssembler.toCommandFromResource(resource, category);

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    // 🔍 GET ALL / SEARCH
    @GetMapping
    public ResponseEntity<List<ProductResource>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId
    ) {

        List<Product> products;

        Category category = null;

        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new TenantMismatchException("Category", categoryId));
            validateCategoryTenant(category);
        }

        if (name != null || category != null) {
            products = queryService.handle(
                    new SearchProductsQuery(name, category)
            );
        } else {
            products = queryService.handle(new GetAllProductsQuery());
        }

        java.util.Map<Long, Long> zoneMap = productKitchenZoneQueryService.getAllProductZoneIds();

        return ResponseEntity.ok(
                products.stream()
                        .map(p -> ProductResourceFromEntityAssembler.toResourceFromEntity(p, zoneMap.get(p.getId())))
                        .toList()
        );
    }

    // 🔍 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResource> getById(@PathVariable Long id) {

        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        Long zoneId = productKitchenZoneQueryService.getZoneIdForProduct(id).orElse(null);

        return ResponseEntity.ok(
                ProductResourceFromEntityAssembler.toResourceFromEntity(product, zoneId)
        );
    }

    // 🔍 GET BY CATEGORY
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResource>> getByCategory(
            @PathVariable Long categoryId
    ) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new TenantMismatchException("Category", categoryId));
        validateCategoryTenant(category);

        List<Product> products = queryService.handle(
                new GetProductsByCategoryQuery(category)
        );

        java.util.Map<Long, Long> zoneMap = productKitchenZoneQueryService.getAllProductZoneIds();

        return ResponseEntity.ok(
                products.stream()
                        .map(p -> ProductResourceFromEntityAssembler.toResourceFromEntity(p, zoneMap.get(p.getId())))
                        .toList()
        );
    }

    // 📊 COUNT BY CATEGORY
    @GetMapping("/categories/count")
    public ResponseEntity<Map<String, Long>> countByCategory() {
        return ResponseEntity.ok(
                queryService.handle(new GetProductCountByCategoryQuery())
        );
    }

    // ✏️ UPDATE
    @PutMapping("/{id}")
    @RequiresPermission("catalog.edit_products_categories")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody UpdateProductResource resource
    ) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        Category category = categoryRepository.findById(resource.categoryId())
                .orElseThrow(() -> new TenantMismatchException("Category", resource.categoryId()));
        validateCategoryTenant(category);

        UpdateProductCommand command = new UpdateProductCommand(
                id,
                resource.name(),
                resource.price(),
                category,
                resource.estimatedPrepTimeMinutes(),
                resource.active()
        );

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    // ❌ DELETE
    @RequiresPermission("catalog.edit_products_categories")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        commandService.handle(new DeleteProductCommand(id));
        return ResponseEntity.ok().build();
    }

    // 🏷️ ASSIGN KITCHEN ZONE
    @PutMapping("/{id}/kitchen-zone")
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<Void> assignKitchenZone(
            @PathVariable Long id,
            @RequestBody AssignProductToZoneResource resource
    ) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        if (resource.zoneId() == null) {
            productKitchenZoneCommandService.removeProductFromZone(id);
        } else {
            productKitchenZoneCommandService.assignProductToZone(id, resource.zoneId());
        }
        return ResponseEntity.ok().build();
    }

    // 🔍 GET KITCHEN ZONE
    @GetMapping("/{id}/kitchen-zone")
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<Map<String, Long>> getKitchenZone(@PathVariable Long id) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        java.util.Optional<Long> zoneIdOpt = productKitchenZoneQueryService.getZoneIdForProduct(id);
        java.util.Map<String, Long> response = new java.util.HashMap<>();
        response.put("zoneId", zoneIdOpt.orElse(null));
        return ResponseEntity.ok(response);
    }

    // 📖 RECIPES ENDPOINTS
    @GetMapping("/{id}/recipe")
    @RequiresPermission("catalog.edit_supplies_recipes")
    public ResponseEntity<List<RecipeResource>> getRecipe(@PathVariable Long id) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        List<Recipe> recipe = recipeQueryService.getRecipeForProduct(id);
        return ResponseEntity.ok(
                recipe.stream()
                        .map(item -> {
                            java.util.Optional<com.pezbackend.inventory.domain.model.entities.Supply> supplyOpt = supplyRepository.findById(item.getSupplyId());
                            String supplyName = supplyOpt.map(com.pezbackend.inventory.domain.model.entities.Supply::getName).orElse("Insumo desconocido");
                            String supplyUnit = supplyOpt.map(com.pezbackend.inventory.domain.model.entities.Supply::getUnit).orElse("u");
                            return RecipeResourceFromEntityAssembler.toResourceFromEntity(item, supplyName, supplyUnit);
                        })
                        .toList()
        );
    }

    @PostMapping("/{id}/recipe")
    @RequiresPermission("catalog.edit_supplies_recipes")
    public ResponseEntity<Void> addRecipeItem(
            @PathVariable Long id,
            @RequestBody AddRecipeItemResource resource
    ) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        recipeCommandService.addOrUpdateRecipeItem(id, resource.supplyId(), resource.quantityUsed());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/recipe")
    @RequiresPermission("catalog.edit_supplies_recipes")
    public ResponseEntity<Void> updateRecipeItem(
            @PathVariable Long id,
            @RequestBody AddRecipeItemResource resource
    ) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        recipeCommandService.addOrUpdateRecipeItem(id, resource.supplyId(), resource.quantityUsed());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/recipe")
    @RequiresPermission("catalog.edit_supplies_recipes")
    public ResponseEntity<Void> deleteRecipeItem(
            @PathVariable Long id,
            @RequestParam Long supplyId
    ) {
        Product product = queryService.handle(new GetProductByIdQuery(id));
        validateProductTenant(product);

        recipeCommandService.deleteRecipeItem(id, supplyId);
        return ResponseEntity.ok().build();
    }
}