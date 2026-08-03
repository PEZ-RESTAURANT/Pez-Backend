package com.pezbackend.catalog.interfaces.rest.transform;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.interfaces.rest.resources.CategoryResource;
import com.pezbackend.catalog.interfaces.rest.resources.ProductResource;

public class ProductResourceFromEntityAssembler {

    public static ProductResource toResourceFromEntity(Product entity) {
        CategoryResource categoryResource = null;
        if (entity.getCategory() != null) {
            categoryResource = new CategoryResource(
                    entity.getCategory().getId(),
                    entity.getCategory().getName()
            );
        }
        return new ProductResource(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                categoryResource,
                entity.getEstimatedPrepTimeMinutes(),
                entity.isActive()
        );
    }
}