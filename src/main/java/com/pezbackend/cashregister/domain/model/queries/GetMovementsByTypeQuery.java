package com.pezbackend.cashregister.domain.model.queries;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record GetMovementsByTypeQuery(
        Long cashRegisterId,
        CashMovementType movementType
) {
    public GetMovementsByTypeQuery {
        if (cashRegisterId == null || cashRegisterId <= 0)
            throw new BadRequestException("CashRegisterId is required");

        if (movementType == null)
            throw new BadRequestException("Movement type is required");
    }
}