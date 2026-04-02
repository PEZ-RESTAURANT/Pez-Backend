package com.pezbackend.catalog.domain.model.commands;

import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        BigDecimal price,
        ProductCategory category
) {}