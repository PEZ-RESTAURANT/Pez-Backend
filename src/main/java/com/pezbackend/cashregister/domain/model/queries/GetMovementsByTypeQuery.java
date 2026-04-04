package com.pezbackend.cashregister.domain.model.queries;

import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;

public record GetMovementsByTypeQuery(Long cashRegisterId, CashMovementType movementType) {}
