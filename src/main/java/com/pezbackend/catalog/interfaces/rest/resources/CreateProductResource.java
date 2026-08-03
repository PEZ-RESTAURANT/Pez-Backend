package com.pezbackend.catalog.interfaces.rest.resources;

import java.math.BigDecimal;

public record CreateProductResource(
        String name,
        BigDecimal price,
        Long categoryId
) {}