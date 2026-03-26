package com.pezbackend.ordering.interfaces.rest.resources;

public record UpdateItemQuantityResource(
        Long itemId,
        Integer quantity
) {}