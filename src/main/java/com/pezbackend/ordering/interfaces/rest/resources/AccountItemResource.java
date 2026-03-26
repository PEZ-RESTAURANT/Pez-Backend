package com.pezbackend.ordering.interfaces.rest.resources;

import java.math.BigDecimal;

public record AccountItemResource(
        Long id,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice,
        String note
) {}