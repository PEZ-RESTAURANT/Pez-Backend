package com.pezbackend.catalog.domain.model.queries;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record GetProductByIdQuery(
        Long productId
) {
    public GetProductByIdQuery {
        if (productId == null || productId <= 0)
            throw new BadRequestException("ProductId is required");
    }
}