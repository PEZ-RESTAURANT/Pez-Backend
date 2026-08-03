package com.pezbackend.catalog.domain.model.queries;

import com.pezbackend.catalog.domain.model.entities.Category;

public record SearchProductsQuery(
        String name,
        Category category
) {}