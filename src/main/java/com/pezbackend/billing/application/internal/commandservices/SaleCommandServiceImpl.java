package com.pezbackend.billing.application.internal.commandservices;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.exceptions.AccountNotClosedException;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class SaleCommandServiceImpl implements SaleCommandService {

    private final SaleRepository saleRepository;
    private final AccountRepository accountRepository;

    public SaleCommandServiceImpl(SaleRepository saleRepository,
                                  AccountRepository accountRepository) {
        this.saleRepository = saleRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Long handle(CreateSaleCommand command) {

        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getStatus().equals(com.pezbackend.ordering.domain.model.valueobjects.AccountStatus.CLOSED)) {
            throw new AccountNotClosedException(account.getId());
        }

        // Creamos la venta directamente con el constructor correcto
        Sale sale = new Sale(
                account.getName(), // o null si no quieres
                account.getStaffId(),
                account.getCustomerName(),
                account.getCustomerDni(),
                account.getCustomerRuc(),
                command.documentType(),
                command.paymentMethod()
        );

        // Agregamos los detalles
        account.getItems().forEach(item ->
                sale.addDetail(item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getNote())
        );

        saleRepository.save(sale);

        return sale.getId();
    }
}