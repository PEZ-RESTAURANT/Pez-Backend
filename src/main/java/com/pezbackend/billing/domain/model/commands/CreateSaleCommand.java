package com.pezbackend.billing.domain.model.commands;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;

import java.util.List;

public record CreateSaleCommand(
        Long accountId,
        DocumentType documentType,
        List<PaymentDetail> payments
) {}