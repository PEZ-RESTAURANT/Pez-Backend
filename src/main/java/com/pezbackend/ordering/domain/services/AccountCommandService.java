package com.pezbackend.ordering.domain.services;

import com.pezbackend.ordering.domain.model.commands.*;

public interface AccountCommandService {

    Long handle(CreateAccountCommand command);

    void handle(AddItemToAccountCommand command);

    void handle(RemoveItemFromAccountCommand command);

    void handle(UpdateItemQuantityCommand command);

    void handle(AssignCustomerToAccountCommand command);

    void handle(NameAccountCommand command);

    void handle(CloseAccountCommand command);

    void handle(IncreaseItemQuantityCommand command);

    void handle(DecreaseItemQuantityCommand command);

    void handle(DeleteAccountCommand command);

    void handle(AddProductToAccountCommand command);

    void handle(MarkAccountAsPaidCommand command);
}