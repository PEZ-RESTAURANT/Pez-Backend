package com.pezbackend.ordering.domain.model.queries;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record GetAccountByIdQuery(Long accountId) {
    public GetAccountByIdQuery {
        if (accountId == null || accountId <= 0)
            throw new BadRequestException("AccountId is required.");
    }
}