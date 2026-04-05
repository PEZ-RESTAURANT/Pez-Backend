package com.pezbackend.catalog.domain.model.queries;

import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record GetProductsByCategoryQuery(
        ProductCategory category
) {
    public GetProductsByCategoryQuery {
        if (category == null)
            throw new BadRequestException("Category is required");
    }
}