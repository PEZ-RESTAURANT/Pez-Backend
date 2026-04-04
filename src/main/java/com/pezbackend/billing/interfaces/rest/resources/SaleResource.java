package com.pezbackend.billing.interfaces.rest.resources;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResource(
        Long id,
        String name,
        Long staffId,
        String customerName,
        String customerDni,
        String customerRuc,
        DocumentType documentType,
        BigDecimal total,
        LocalDateTime createdAt,
        List<SaleDetailResource> details,
        List<SalePaymentResource> payments
) {}