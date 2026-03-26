package com.pezbackend.ordering.interfaces.rest.resources;

import java.math.BigDecimal;

public record AddItemToAccountResource(
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        String note
) {}