package com.pezbackend.catalog.interfaces.rest.resources;

import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;

import java.math.BigDecimal;
import java.util.Date;

public record ProductResource(
        Long id,
        String name,
        BigDecimal price,
        ProductCategory category,
        Date createdAt,
        Date updatedAt
) {}