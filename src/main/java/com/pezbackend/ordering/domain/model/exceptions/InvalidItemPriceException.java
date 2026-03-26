package com.pezbackend.ordering.domain.model.exceptions;

import java.math.BigDecimal;

public class InvalidItemPriceException extends RuntimeException {
    public InvalidItemPriceException(BigDecimal price) {
        super("Invalid item price: " + price + ". Price must be greater than 0.");
    }
}