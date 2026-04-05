package com.pezbackend.billing.application.internal.commandservices;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.exceptions.AccountNotClosedException;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.cashregister.domain.model.commands.AddSaleIncomeCommand;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.ordering.domain.model.commands.MarkAccountAsPaidCommand;
import com.pezbackend.ordering.domain.model.queries.GetAccountByIdQuery;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import com.pezbackend.ordering.domain.services.AccountCommandService;
import com.pezbackend.ordering.domain.services.AccountQueryService;
import org.springframework.stereotype.Service;

@Service
public class SaleCommandServiceImpl implements SaleCommandService {

    private final SaleRepository saleRepository;
    private final AccountQueryService accountQueryService;
    private final AccountCommandService accountCommandService;
    private final CashRegisterCommandService cashRegisterCommandService;

    public SaleCommandServiceImpl(SaleRepository saleRepository,
                                  AccountQueryService accountQueryService,
                                  AccountCommandService accountCommandService,
                                  CashRegisterCommandService cashRegisterCommandService) {
        this.saleRepository = saleRepository;
        this.accountQueryService = accountQueryService;
        this.accountCommandService = accountCommandService;
        this.cashRegisterCommandService = cashRegisterCommandService;
    }

    @Override
    public Long handle(CreateSaleCommand command) {

        var account = accountQueryService.handle(
                new GetAccountByIdQuery(command.accountId())
        );

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
                command.documentType()
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

        // 🔥 pagos combinados
        command.payments().forEach(p ->
                sale.addPayment(p.method(), p.amount())
        );

        // 🔥 validar que pagos == total
        sale.validatePayments();

        saleRepository.save(sale);

        // 🔥 cambiar estado cuenta usando command service
        accountCommandService.handle(
                new MarkAccountAsPaidCommand(account.getId())
        );

        // 🔥 registrar SOLO efectivo en caja
        command.payments().forEach(p -> {
            if (p.method() == PaymentMethod.CASH) {
                cashRegisterCommandService.handle(
                        new AddSaleIncomeCommand(
                                p.amount(),
                                "Ingreso por venta: " + sale.getId()
                        )
                );
            }
        });

        return sale.getId();
    }
}