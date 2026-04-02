package com.pezbackend.catalog.interfaces.rest.resources;

import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;

import java.math.BigDecimal;

public record CreateProductResource(
        String name,
        BigDecimal price,
        ProductCategory category
) {}