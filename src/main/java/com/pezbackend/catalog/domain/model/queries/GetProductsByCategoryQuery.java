package com.pezbackend.catalog.domain.model.queries;

import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record GetProductsByCategoryQuery(
        Category category
) {
    public GetProductsByCategoryQuery {
        if (category == null)
            throw new BadRequestException("Category is required");
    }
}