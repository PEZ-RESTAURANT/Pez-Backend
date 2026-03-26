package com.pezbackend.billing.domain.services;

import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;

public interface SaleCommandService {
    Long handle(CreateSaleCommand command);
}
