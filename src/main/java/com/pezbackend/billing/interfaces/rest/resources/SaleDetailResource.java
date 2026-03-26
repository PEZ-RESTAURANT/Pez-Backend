package com.pezbackend.billing.interfaces.rest.resources;

import java.math.BigDecimal;

public record SaleDetailResource(
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice,
        String note
) {}