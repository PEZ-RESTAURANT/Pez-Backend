package com.pezbackend.billing.interfaces.rest.transform;

import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.interfaces.rest.resources.CreateSaleResource;
import java.util.stream.Collectors;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import com.pezbackend.billing.interfaces.rest.resources.PaymentDetailResource;

public class CreateSaleCommandFromResourceAssembler {

    public static CreateSaleCommand toCommandFromResource(CreateSaleResource resource) {
        return new CreateSaleCommand(
                resource.accountId(),
                resource.documentType(),
                resource.payments().stream()
                        .map(CreateSaleCommandFromResourceAssembler::toPaymentDetail)
                        .collect(Collectors.toList())
        );
    }

    private static PaymentDetail toPaymentDetail(PaymentDetailResource resource) {
        return new PaymentDetail(
                resource.method(),
                resource.amount()
        );
    }
}