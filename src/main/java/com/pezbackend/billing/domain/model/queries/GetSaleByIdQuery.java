package com.pezbackend.billing.domain.model.queries;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record GetSaleByIdQuery(Long id) {
    public GetSaleByIdQuery {
        if (id == null || id <= 0)
            throw new BadRequestException("Sale id is required");
    }
}
