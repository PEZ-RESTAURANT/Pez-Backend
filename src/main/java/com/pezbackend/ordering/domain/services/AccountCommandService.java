package com.pezbackend.ordering.domain.services;

import com.pezbackend.ordering.domain.model.commands.MarkAccountAsPaidCommand;

// TODO Fase 5: Eliminar esta clase/interfaz cuando el módulo billing se integre con com.pezbackend.orders
public interface AccountCommandService {
    void handle(MarkAccountAsPaidCommand command);
}