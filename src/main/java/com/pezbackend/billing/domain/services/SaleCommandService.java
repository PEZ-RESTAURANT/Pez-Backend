package com.pezbackend.billing.domain.services;

import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import java.util.List;

public interface SaleCommandService {
    Long handle(CreateSaleCommand command);
    void registerPayments(Long saleId, List<PaymentDetail> payments, String executor);
    void voidSale(Long saleId, String reason, String executor);
}
