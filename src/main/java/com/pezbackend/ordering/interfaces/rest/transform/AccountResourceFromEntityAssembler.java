package com.pezbackend.ordering.interfaces.rest.transform;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.entities.AccountItem;
import com.pezbackend.ordering.interfaces.rest.resources.AccountItemResource;
import com.pezbackend.ordering.interfaces.rest.resources.AccountResource;

import java.util.List;

public class AccountResourceFromEntityAssembler {

    public static AccountResource toResourceFromEntity(Account entity) {

        List<AccountItemResource> items = entity.getItems()
                .stream()
                .map(AccountResourceFromEntityAssembler::toItemResource)
                .toList();

        return new AccountResource(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getStaffId(),
                entity.getCustomerName(),
                entity.getCustomerDni(),
                entity.getCustomerRuc(),
                entity.getTotal(),
                items,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static AccountItemResource toItemResource(AccountItem item) {
        return new AccountItemResource(
                item.getId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalPrice(),
                item.getNote()
        );
    }
}