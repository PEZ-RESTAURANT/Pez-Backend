package com.pezbackend.catalog.interfaces.rest.resources;

import java.math.BigDecimal;

public record ProductResource(
        Long id,
        String name,
        BigDecimal price,
        CategoryResource category,
        Integer estimatedPrepTimeMinutes,
        Boolean active,
        Long kitchenZoneId
) {}