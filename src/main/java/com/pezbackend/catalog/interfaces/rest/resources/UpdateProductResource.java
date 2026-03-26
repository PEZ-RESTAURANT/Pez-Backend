package com.pezbackend.catalog.interfaces.rest.resources;

import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;

import java.math.BigDecimal;

public record UpdateProductResource(
        String name,
        BigDecimal price,
        ProductCategory category
) {}