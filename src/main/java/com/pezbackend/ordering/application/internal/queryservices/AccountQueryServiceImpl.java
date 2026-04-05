package com.pezbackend.ordering.application.internal.queryservices;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.exceptions.AccountNotFoundException;
import com.pezbackend.ordering.domain.model.queries.*;
import com.pezbackend.ordering.domain.services.AccountQueryService;
import com.pezbackend.ordering.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountRepository accountRepository;

    public AccountQueryServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account handle(GetAccountByIdQuery query) {
        return accountRepository.findById(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId()));
    }

    @Override
    public List<Account> handle(GetAllAccountsQuery query) {
        return accountRepository.findAll();
    }

    @Override
    public List<Account> handle(GetAccountsByStatusQuery query) {
        return accountRepository.findByStatus(query.status());
    }
}