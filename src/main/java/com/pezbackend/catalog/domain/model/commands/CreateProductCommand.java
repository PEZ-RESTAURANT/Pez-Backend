package com.pezbackend.catalog.domain.model.commands;

import com.pezbackend.catalog.domain.model.exceptions.InvalidProductCategoryException;
import com.pezbackend.catalog.domain.model.exceptions.InvalidProductNameException;
import com.pezbackend.catalog.domain.model.exceptions.InvalidProductPriceException;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        BigDecimal price,
        ProductCategory category
) {
    public CreateProductCommand {
        if (name == null || name.isBlank())
            throw new InvalidProductNameException();

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidProductPriceException(price);

        if (category == null)
            throw new InvalidProductCategoryException();
    }
}