package com.pezbackend.catalog.domain.model.queries;

import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;

public record GetProductsByCategoryQuery(
        ProductCategory category
) {}