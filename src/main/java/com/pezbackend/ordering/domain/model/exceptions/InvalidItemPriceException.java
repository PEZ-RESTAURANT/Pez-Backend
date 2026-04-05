package com.pezbackend.ordering.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.math.BigDecimal;

public class InvalidItemPriceException extends BadRequestException {
    public InvalidItemPriceException(BigDecimal price) {
        super("Invalid item price: " + price + ". Must be > 0");
    }
}