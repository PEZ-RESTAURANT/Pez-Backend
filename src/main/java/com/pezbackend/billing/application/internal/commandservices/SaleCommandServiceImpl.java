package com.pezbackend.billing.application.internal.commandservices;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.exceptions.AccountNotClosedException;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import com.pezbackend.ordering.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class SaleCommandServiceImpl implements SaleCommandService {

    private final SaleRepository saleRepository;
    private final AccountRepository accountRepository;
    private final CashRegisterCommandService cashRegisterCommandService;

    public SaleCommandServiceImpl(SaleRepository saleRepository,
                                  AccountRepository accountRepository,
                                  CashRegisterCommandService cashRegisterCommandService) {
        this.saleRepository = saleRepository;
        this.accountRepository = accountRepository;
        this.cashRegisterCommandService = cashRegisterCommandService;
    }

    @Override
    public Long handle(CreateSaleCommand command) {

        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getStatus().equals(
                AccountStatus.PAYMENT_PENDING)) {
            throw new AccountNotClosedException(account.getId());
        }

        // 🔥 Crear venta
        Sale sale = new Sale(
                account.getName(),
                account.getStaffId(),
                account.getCustomerName(),
                account.getCustomerDni(),
                account.getCustomerRuc(),
                command.documentType(),
                command.paymentMethod()
        );

        // 🔥 Agregar detalles
        account.getItems().forEach(item ->
                sale.addDetail(
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getNote()
                )
        );

        saleRepository.save(sale);

        account.markAsPaid();
        accountRepository.save(account);

        // 💰🔥 REGISTRAR EN CAJA AUTOMÁTICAMENTE
        if (command.paymentMethod() ==
                com.pezbackend.billing.domain.model.valueobjects.PaymentMethod.CASH) {

            cashRegisterCommandService.handle(
                    new com.pezbackend.cashregister.domain.model.commands.AddSaleIncomeCommand(
                            account.getId()
                    )
            );
        }

        return sale.getId();
    }
}