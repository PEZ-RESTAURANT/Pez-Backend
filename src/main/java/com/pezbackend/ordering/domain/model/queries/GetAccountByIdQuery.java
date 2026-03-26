package com.pezbackend.ordering.domain.model.queries;

public record GetAccountByIdQuery(Long accountId) {
    public GetAccountByIdQuery {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("AccountId is required.");
    }
}