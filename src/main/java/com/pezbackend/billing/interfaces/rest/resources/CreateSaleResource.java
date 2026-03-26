package com.pezbackend.billing.interfaces.rest.resources;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;


public record CreateSaleResource(
        Long accountId,
        DocumentType documentType,
        PaymentMethod paymentMethod
) {}