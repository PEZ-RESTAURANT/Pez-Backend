package com.pezbackend.billing.interfaces.rest.resources;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record SaleResource(
        Long id,
        String name,
        Long staffId,
        String customerName,
        String customerDni,
        String customerRuc,
        DocumentType documentType,
        PaymentMethod paymentMethod,
        BigDecimal total,
        Date createdAt,
        List<SaleDetailResource> details
) {}