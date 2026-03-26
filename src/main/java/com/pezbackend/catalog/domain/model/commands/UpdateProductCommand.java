package com.pezbackend.catalog.domain.model.commands;

import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;

import java.math.BigDecimal;

public record UpdateProductCommand(
        Long productId,
        String name,
        BigDecimal price,
        ProductCategory category
) {}