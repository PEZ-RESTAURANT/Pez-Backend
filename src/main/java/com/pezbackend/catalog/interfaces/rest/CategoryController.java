package com.pezbackend.catalog.interfaces.rest;

import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.interfaces.rest.resources.CategoryResource;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.shared.domain.exceptions.TenantMismatchException;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    private void validateTenant(Category category) {
        Long currentTenant = TenantContext.getCurrentTenantId();
        if (currentTenant != null && !currentTenant.equals(category.getRestaurantId())) {
            throw new TenantMismatchException("Category", category.getId());
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryResource>> getAll() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(
                categories.stream()
                        .map(c -> new CategoryResource(c.getId(), c.getName()))
                        .toList()
        );
    }

    @PostMapping
    @RequiresPermission("catalog.edit_products_categories")
    public ResponseEntity<CategoryResource> create(@RequestBody CategoryResource resource) {
        if (resource.name() == null || resource.name().isBlank()) {
            throw new BadRequestException("El nombre de la categoría es obligatorio.");
        }

        if (categoryRepository.existsByNameIgnoreCase(resource.name().trim())) {
            throw new BadRequestException("Ya existe una categoría con ese nombre.");
        }

        Category category = new Category(resource.name().trim());
        categoryRepository.save(category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CategoryResource(category.getId(), category.getName()));
    }

    @PutMapping("/{id}")
    @RequiresPermission("catalog.edit_products_categories")
    public ResponseEntity<CategoryResource> update(@PathVariable Long id, @RequestBody CategoryResource resource) {
        if (resource.name() == null || resource.name().isBlank()) {
            throw new BadRequestException("El nombre de la categoría es obligatorio.");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new TenantMismatchException("Category", id));
        validateTenant(category);

        if (categoryRepository.existsByNameIgnoreCase(resource.name().trim()) &&
                !category.getName().equalsIgnoreCase(resource.name().trim())) {
            throw new BadRequestException("Ya existe otra categoría con ese nombre.");
        }

        category.setName(resource.name().trim());
        categoryRepository.save(category);

        return ResponseEntity.ok(new CategoryResource(category.getId(), category.getName()));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("catalog.edit_products_categories")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new TenantMismatchException("Category", id));
        validateTenant(category);

        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("No se puede eliminar una categoría con productos activos.");
        }

        categoryRepository.delete(category);
        return ResponseEntity.ok().build();
    }
}
