package com.pezbackend.billing.interfaces.rest.resources;

import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import java.math.BigDecimal;

public record PaymentDetailResource(
        PaymentMethod method,
        BigDecimal amount
) {}