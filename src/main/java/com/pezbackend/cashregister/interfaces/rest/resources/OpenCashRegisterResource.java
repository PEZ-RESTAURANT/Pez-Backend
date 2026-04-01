package com.pezbackend.cashregister.interfaces.rest.resources;

import java.math.BigDecimal;

public record OpenCashRegisterResource(
        BigDecimal openingBalance
) {}