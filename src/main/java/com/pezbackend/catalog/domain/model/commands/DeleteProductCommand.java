package com.pezbackend.catalog.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record DeleteProductCommand(
        Long productId
) {
    public DeleteProductCommand {
        if (productId == null || productId <= 0)
            throw new BadRequestException("ProductId is required");
    }
}