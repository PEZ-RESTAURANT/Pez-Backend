package com.pezbackend.billing.interfaces.rest.resources;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representación DTO para el recurso Sale.
 */
public record SaleResource(
        Long id,
        String name,
        Long staffId,
        String customerName,
        String customerDocumentNumber,
        DocumentType documentType,
        String saleStatus,
        Long orderId,
        BigDecimal total,
        LocalDateTime createdAt,
        List<SaleDetailResource> details,
        List<SalePaymentResource> payments
) {}