package com.pezbackend.billing.domain.model.queries;

import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;

public record GetSalesByPaymentMethodQuery(PaymentMethod paymentMethod) {
}
