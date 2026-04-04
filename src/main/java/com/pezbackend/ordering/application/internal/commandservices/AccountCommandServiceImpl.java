package com.pezbackend.ordering.application.internal.commandservices;

import com.pezbackend.catalog.domain.model.queries.GetProductByIdQuery;
import com.pezbackend.catalog.domain.services.ProductQueryService;
import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.commands.*;
import com.pezbackend.ordering.domain.model.exceptions.AccountNameAlreadyExistsException;
import com.pezbackend.ordering.domain.model.exceptions.AccountNotFoundException;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import com.pezbackend.ordering.domain.services.AccountCommandService;
import com.pezbackend.ordering.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountCommandServiceImpl implements AccountCommandService {

    private final AccountRepository accountRepository;
    private final ProductQueryService productQueryService;

    private static final List<AccountStatus> ACTIVE_STATUSES = List.of(
            AccountStatus.OPEN,
            AccountStatus.IN_PROGRESS
    );

    public AccountCommandServiceImpl(AccountRepository accountRepository, ProductQueryService productQueryService) {
        this.accountRepository = accountRepository;
        this.productQueryService = productQueryService;
    }

    @Override
    @Transactional
    public Long handle(CreateAccountCommand command) {
        Account account = new Account(command.staffId());
        accountRepository.save(account);
        return account.getId();
    }

    @Override
    @Transactional
    public void handle(AddItemToAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.addItem(
                command.productName(),
                command.unitPrice(),
                command.quantity(),
                command.note()
        );

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void handle(RemoveItemFromAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.removeItem(command.itemId());

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void handle(UpdateItemQuantityCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.updateItemQuantity(command.itemId(), command.quantity());
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void handle(AssignCustomerToAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.assignCustomer(
                command.customerName(),
                command.dni(),
                command.ruc()
        );

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void handle(NameAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        // 🔥 Validar nombres duplicados en cuentas activas
        boolean exists = accountRepository.existsByNameIgnoreCaseAndStatusIn(
                command.name(),
                ACTIVE_STATUSES
        );

        // VERIFY
        if (exists && (account.getName() == null ||
                !account.getName().equalsIgnoreCase(command.name()))) {

            throw new AccountNameAlreadyExistsException(command.name());
        }

        String normalizedName = command.name().trim();

        account.assignName(normalizedName);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void handle(CloseAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.closeAccount();
        accountRepository.save(account);
    }

    @Override
    public void handle(IncreaseItemQuantityCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.increaseItemQuantity(command.itemId());

        accountRepository.save(account);
    }

    @Override
    public void handle(DecreaseItemQuantityCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.decreaseItemQuantity(command.itemId());

        accountRepository.save(account);
    }

    @Override
    public void handle(CancelAccountCommand command) {

        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.cancelAccount();

        accountRepository.save(account);
    }

    @Override
    public void handle(AddProductToAccountCommand command) {

        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        var product = productQueryService.handle(
                new GetProductByIdQuery(command.productId())
        );

        account.addItem(
                product.getName(),
                product.getPrice(),
                1,
                ""
        );

        accountRepository.save(account);
    }

    @Override
    public void handle(MarkAccountAsPaidCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.markAsPaid();

        accountRepository.save(account);
    }
}