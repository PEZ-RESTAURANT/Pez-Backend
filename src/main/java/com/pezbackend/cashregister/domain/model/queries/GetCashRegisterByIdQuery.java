package com.pezbackend.cashregister.domain.model.queries;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record GetCashRegisterByIdQuery(Long cashRegisterId) {
    public GetCashRegisterByIdQuery {
        if (cashRegisterId == null || cashRegisterId <= 0)
            throw new BadRequestException("CashRegisterId is required");
    }
}