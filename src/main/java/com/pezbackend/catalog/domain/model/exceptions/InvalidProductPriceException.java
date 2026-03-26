package com.pezbackend.catalog.domain.model.exceptions;

import java.math.BigDecimal;

public class InvalidProductPriceException extends RuntimeException {
    public InvalidProductPriceException(BigDecimal price) {
        super("Invalid product price: " + price);
    }
}