package com.pezbackend.cashregister.interfaces.rest.transform;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.interfaces.rest.resources.CashMovementResource;
import com.pezbackend.cashregister.interfaces.rest.resources.CashRegisterResource;

import java.util.stream.Collectors;

public class CashRegisterResourceAssembler {

    public static CashRegisterResource toResource(CashRegister cashRegister) {
        return new CashRegisterResource(
                cashRegister.getId(),
                cashRegister.getOpeningBalance(),
                cashRegister.getCurrentBalance(),
                cashRegister.getStatus().name(),
                cashRegister.getMovements().stream()
                        .map(CashMovementResourceAssembler::toResource)
                        .collect(Collectors.toList())
        );
    }
}