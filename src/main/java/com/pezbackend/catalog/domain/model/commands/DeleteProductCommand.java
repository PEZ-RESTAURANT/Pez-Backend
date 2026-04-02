package com.pezbackend.catalog.domain.model.commands;

public record DeleteProductCommand(
        Long productId
) {}