package com.pezbackend.billing.domain.model.commands;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;

public record CreateSaleCommand(
        Long accountId,           // para transformar la cuenta cerrada en venta
        DocumentType documentType,
        PaymentMethod paymentMethod
) {}