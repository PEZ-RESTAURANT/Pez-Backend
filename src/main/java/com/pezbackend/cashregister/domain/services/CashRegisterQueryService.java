package com.pezbackend.cashregister.domain.services;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.queries.*;
import com.pezbackend.cashregister.domain.model.valueobjects.MovementsSummary;

import java.util.List;

public interface CashRegisterQueryService {

    CashRegister handle(GetCurrentCashRegisterQuery query);

    CashRegister handle(GetCashRegisterByIdQuery query);

    List<CashMovement> handle(GetMovementsByTypeQuery query);

    MovementsSummary handle(GetMovementsSummaryQuery query);

    List<CashRegister> handle(GetCashRegistersByDateRangeQuery query);
}