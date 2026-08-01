package com.pezbackend.ordering.application.internal.queryservices;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.entities.AccountItem;
import com.pezbackend.ordering.domain.model.queries.GetAccountByIdQuery;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import com.pezbackend.ordering.domain.services.AccountQueryService;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.valueobjects.OrderStatus;
import com.pezbackend.orders.domain.services.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

// TODO Fase 5: Eliminar esta clase/interfaz cuando el módulo billing se integre con com.pezbackend.orders
@Service
@RequiredArgsConstructor
public class AccountQueryServiceImpl implements AccountQueryService {

    private final OrderQueryService orderQueryService;
    private final ProductRepository productRepository;

    @Override
    public Account handle(GetAccountByIdQuery query) {
        Order order = orderQueryService.getOrderById(query.accountId());

        Account account = new Account();
        account.setId(order.getId());
        account.setName(order.getTableId() != null ? "Mesa " + order.getTableId() : "Pedido " + order.getId());
        
        Long waiterId = order.getItems().isEmpty() ? 1L : order.getItems().get(0).getWaiterId();
        account.setStaffId(waiterId);
        account.setCustomerName("Cliente " + (order.getCustomerId() != null ? order.getCustomerId() : ""));
        account.setCustomerDni("");
        account.setCustomerRuc("");

        if (order.getStatus() == OrderStatus.ISSUED_UNPAID) {
            account.setStatus(AccountStatus.PAYMENT_PENDING);
        } else if (order.getStatus() == OrderStatus.PAID) {
            account.setStatus(AccountStatus.PAID);
        } else {
            account.setStatus(AccountStatus.OPEN);
        }

        var items = new ArrayList<AccountItem>();
        order.getItems().forEach(item -> {
            AccountItem accountItem = new AccountItem();
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            accountItem.setProductName(product != null ? product.getName() : "Plato " + item.getProductId());
            accountItem.setUnitPrice(item.getUnitPriceSnapshot());
            accountItem.setQuantity(item.getQuantity());
            accountItem.setNote(item.getNote());
            items.add(accountItem);
        });
        account.setItems(items);

        return account;
    }
}