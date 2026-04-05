package com.pezbackend.catalog.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

import java.math.BigDecimal;

public class InvalidProductPriceException extends BusinessRuleException {
    public InvalidProductPriceException(BigDecimal price) {
        super("Invalid product price: " + price);
    }
}