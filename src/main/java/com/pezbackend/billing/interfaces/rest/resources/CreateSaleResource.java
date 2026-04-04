package com.pezbackend.billing.interfaces.rest.resources;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;

import java.util.List;

public record CreateSaleResource(
        Long accountId,
        DocumentType documentType,
        List<PaymentDetailResource> payments
) {}