package com.pezbackend.billing.domain.model.commands;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.util.List;

public record CreateSaleCommand(
        Long accountId,
        DocumentType documentType,
        List<PaymentDetail> payments
) {
    public CreateSaleCommand {
        if (accountId == null || accountId <= 0)
            throw new BadRequestException("AccountId is required");

        if (documentType == null)
            throw new BadRequestException("Document type is required");

        if (payments == null || payments.isEmpty())
            throw new BadRequestException("Payments are required");
    }
}