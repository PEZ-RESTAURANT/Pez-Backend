package com.pezbackend.catalog.interfaces.rest.transform;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.interfaces.rest.resources.ProductResource;

public class ProductResourceFromEntityAssembler {

    public static ProductResource toResourceFromEntity(Product entity) {
        return new ProductResource(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getCategory(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}