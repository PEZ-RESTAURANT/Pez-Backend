package com.pezbackend.ordering.interfaces.rest;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.commands.*;
import com.pezbackend.ordering.domain.model.queries.GetAccountByIdQuery;
import com.pezbackend.ordering.domain.model.queries.GetAccountsByStatusQuery;
import com.pezbackend.ordering.domain.model.queries.GetAllAccountsQuery;
import com.pezbackend.ordering.domain.services.AccountCommandService;
import com.pezbackend.ordering.domain.services.AccountQueryService;
import com.pezbackend.ordering.interfaces.rest.resources.*;
import com.pezbackend.ordering.interfaces.rest.transform.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountCommandService commandService;
    private final AccountQueryService queryService;

    public AccountController(AccountCommandService commandService,
                             AccountQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    // 🔥 CREATE ACCOUNT
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CreateAccountResource resource) {

        CreateAccountCommand command =
                CreateAccountCommandFromResourceAssembler.toCommandFromResource(resource);

        Long accountId = commandService.handle(command);
        return ResponseEntity.ok(accountId);
    }

    // 🔍 GET ALL / FILTER BY STATUS
    @GetMapping
    public ResponseEntity<List<AccountResource>> getAll(
            @RequestParam(required = false) String status
    ) {

        List<Account> accounts;

        if (status != null) {
            accounts = queryService.handle(
                    new GetAccountsByStatusQuery(
                            Enum.valueOf(
                                    com.pezbackend.ordering.domain.model.valueobjects.AccountStatus.class,
                                    status
                            )
                    )
            );
        } else {
            accounts = queryService.handle(new GetAllAccountsQuery());
        }

        return ResponseEntity.ok(
                accounts.stream()
                        .map(AccountResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    // 🔍 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<AccountResource> getById(@PathVariable Long id) {

        Account account =
                queryService.handle(new GetAccountByIdQuery(id));

        return ResponseEntity.ok(
                AccountResourceFromEntityAssembler.toResourceFromEntity(account)
        );
    }

    // ➕ ADD ITEM
    @PostMapping("/{id}/items")
    public ResponseEntity<Void> addItem(
            @PathVariable Long id,
            @RequestBody AddItemToAccountResource resource
    ) {

        AddItemToAccountCommand command =
                AddItemToAccountCommandFromResourceAssembler
                        .toCommandFromResource(id, resource);

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{accountId}/items/{itemId}")
    public ResponseEntity<?> removeItem(
            @PathVariable Long accountId,
            @PathVariable Long itemId
    ) {
        RemoveItemFromAccountCommand command =
                new RemoveItemFromAccountCommand(accountId, itemId);

        commandService.handle(command);

        return ResponseEntity.ok().build();
    }

    // ✏️ UPDATE ITEM QUANTITY
    @PutMapping("/{id}/items")
    public ResponseEntity<Void> updateItemQuantity(
            @PathVariable Long id,
            @RequestBody UpdateItemQuantityResource resource
    ) {

        UpdateItemQuantityCommand command =
                UpdateItemQuantityCommandFromResourceAssembler
                        .toCommandFromResource(id, resource);

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountId}/items/{itemId}/increase")
    public ResponseEntity<?> increaseItemQuantity(
            @PathVariable Long accountId,
            @PathVariable Long itemId
    ) {
        IncreaseItemQuantityCommand command =
                new IncreaseItemQuantityCommand(accountId, itemId);

        commandService.handle(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountId}/items/{itemId}/decrease")
    public ResponseEntity<?> decreaseItemQuantity(
            @PathVariable Long accountId,
            @PathVariable Long itemId
    ) {
        DecreaseItemQuantityCommand command =
                new DecreaseItemQuantityCommand(accountId, itemId);

        commandService.handle(command);

        return ResponseEntity.ok().build();
    }

    // 👤 ASSIGN CUSTOMER
    @PostMapping("/{id}/customer")
    public ResponseEntity<Void> assignCustomer(
            @PathVariable Long id,
            @RequestBody AssignCustomerResource resource
    ) {

        AssignCustomerToAccountCommand command =
                AssignCustomerCommandFromResourceAssembler
                        .toCommandFromResource(id, resource);

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    // 🏷️ NAME ACCOUNT
    @PostMapping("/{id}/name")
    public ResponseEntity<Void> nameAccount(
            @PathVariable Long id,
            @RequestBody NameAccountResource resource
    ) {

        NameAccountCommand command =
                NameAccountCommandFromResourceAssembler
                        .toCommandFromResource(id, resource);

        commandService.handle(command);
        return ResponseEntity.ok().build();
    }

    // 🔒 CLOSE ACCOUNT
    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id) {

        commandService.handle(new CloseAccountCommand(id));
        return ResponseEntity.ok().build();
    }
}