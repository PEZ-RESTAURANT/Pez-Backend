package com.pezbackend.ordering.application.internal.commandservices;

import com.pezbackend.ordering.domain.model.commands.MarkAccountAsPaidCommand;
import com.pezbackend.ordering.domain.services.AccountCommandService;
import com.pezbackend.orders.domain.services.OrderCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// TODO Fase 5: Eliminar esta clase/interfaz cuando el módulo billing se integre con com.pezbackend.orders
@Service
@RequiredArgsConstructor
public class AccountCommandServiceImpl implements AccountCommandService {

    private final OrderCommandService orderCommandService;

    @Override
    public void handle(MarkAccountAsPaidCommand command) {
        orderCommandService.markAsPaid(command.accountId());
    }
}