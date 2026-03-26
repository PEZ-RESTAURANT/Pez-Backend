package com.pezbackend.ordering.domain.services;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.queries.*;

import java.util.List;

public interface AccountQueryService {
    Account handle(GetAccountByIdQuery query);
    List<Account> handle(GetAllAccountsQuery query);
    List<Account> handle(GetAccountsByStatusQuery query);
}