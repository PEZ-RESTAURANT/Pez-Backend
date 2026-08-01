package com.pezbackend.ordering.domain.services;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.queries.GetAccountByIdQuery;

// TODO Fase 5: Eliminar esta clase/interfaz cuando el módulo billing se integre con com.pezbackend.orders
public interface AccountQueryService {
    Account handle(GetAccountByIdQuery query);
}