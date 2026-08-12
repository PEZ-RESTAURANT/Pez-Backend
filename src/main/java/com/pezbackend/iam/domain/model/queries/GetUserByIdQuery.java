package com.pezbackend.iam.domain.model.queries;

public record GetUserByIdQuery(Long userId) {
    public GetUserByIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }
}
