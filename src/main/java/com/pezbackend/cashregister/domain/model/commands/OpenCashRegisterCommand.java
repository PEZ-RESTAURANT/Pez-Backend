package com.pezbackend.cashregister.domain.model.commands;

import java.math.BigDecimal;

public record OpenCashRegisterCommand(BigDecimal openingBalance) {}