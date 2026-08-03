package com.pezbackend.catalog.interfaces.rest.resources;

import java.math.BigDecimal;

public record UpdateProductResource(
        String name,
        BigDecimal price,
        Long categoryId,
        Integer estimatedPrepTimeMinutes,
        Boolean active
) {}