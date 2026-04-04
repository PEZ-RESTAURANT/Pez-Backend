package com.pezbackend.billing.domain.model.valueobjects;

import java.math.BigDecimal;

public record PaymentDetail(
        PaymentMethod method,
        BigDecimal amount
) {}