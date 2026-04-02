package com.pezbackend.cashregister.domain.services;

import com.pezbackend.cashregister.domain.model.commands.*;

public interface CashRegisterCommandService {

    Long handle(OpenCashRegisterCommand command);

    void handle(CloseCashRegisterCommand command);

    Long handle(AddCashMovementCommand command);

    Long handle(AddSaleIncomeCommand command);

}