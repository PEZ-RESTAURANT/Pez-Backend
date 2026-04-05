package com.pezbackend.billing.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;

public class PaymentMismatchException extends BusinessRuleException {
    public PaymentMismatchException() {
        super("Payment total must equal sale total");
    }
}
