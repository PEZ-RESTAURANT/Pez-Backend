package com.pezbackend.catalog.interfaces.rest;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.commands.CreateProductCommand;
import com.pezbackend.catalog.domain.model.commands.DeleteProductCommand;
import com.pezbackend.catalog.domain.model.commands.UpdateProductCommand;
import com.pezbackend.catalog.domain.model.queries.*;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import com.pezbackend.catalog.domain.services.ProductCommandService;
import com.pezbackend.catalog.domain.services.ProductQueryService;
import com.pezbackend.catalog.interfaces.rest.resources.CreateProductResource;
import com.pezbackend.catalog.interfaces.rest.resources.ProductResource;
import com.pezbackend.catalog.interfaces.rest.resources.UpdateProductResource;
import com.pezbackend.catalog.interfaces.rest.transform.CreateProductCommandFromResourceAssembler;
import com.pezbackend.catalog.interfaces.rest.transform.ProductResourceFromEntityAssembler;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.AuthorizeRoles;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductCommandService commandService;
    private final ProductQueryService queryService;

    public ProductController(ProductCommandService commandService,
                             ProductQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    // 🔥 CREATE
    @PreAuthorize(AuthorizeRoles.ADMIN)
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateProductResource resource) {

        CreateProductCommand command =
                CreateProductCommandFromResourceAssembler.toCommandFromResource(resource);

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    // 🔍 GET ALL / SEARCH
    @GetMapping
    public ResponseEntity<List<ProductResource>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category
    ) {

        List<Product> products;

        ProductCategory parsedCategory = null;

        if (category != null) {
            parsedCategory = ProductCategory.valueOf(category);
        }

        if (name != null || parsedCategory != null) {
            products = queryService.handle(
                    new SearchProductsQuery(name, parsedCategory)
            );
        } else {
            products = queryService.handle(new GetAllProductsQuery());
        }

        return ResponseEntity.ok(
                products.stream()
                        .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    // 🔍 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResource> getById(@PathVariable Long id) {

        Product product = queryService.handle(new GetProductByIdQuery(id));

        return ResponseEntity.ok(
                ProductResourceFromEntityAssembler.toResourceFromEntity(product)
        );
    }

    // 🔍 GET BY CATEGORY
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResource>> getByCategory(
            @PathVariable String category
    ) {

        ProductCategory parsedCategory = ProductCategory.valueOf(category);

        List<Product> products = queryService.handle(
                new GetProductsByCategoryQuery(parsedCategory)
        );

        return ResponseEntity.ok(
                products.stream()
                        .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
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
    @PreAuthorize(AuthorizeRoles.ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody UpdateProductResource resource
    ) {

        UpdateProductCommand command = new UpdateProductCommand(
                id,
                resource.name(),
                resource.price(),
                resource.category()
        );

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    // ❌ DELETE
    @PreAuthorize(AuthorizeRoles.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        commandService.handle(new DeleteProductCommand(id));
        return ResponseEntity.ok().build();
    }
}