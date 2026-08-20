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
        String cashierName,
        String waiterName,
        String customerName,
        String customerDocumentNumber,
        DocumentType documentType,
        String saleStatus,
        Long orderId,
        LocalDateTime orderCreatedAt,
        LocalDateTime orderDeliveredAt,
        String ticketNumber,
        BigDecimal total,
        LocalDateTime createdAt,
        List<SaleDetailResource> details,
        List<SalePaymentResource> payments,
        String voidedReason,
        String voidedBy,
        LocalDateTime voidedAt
) {}