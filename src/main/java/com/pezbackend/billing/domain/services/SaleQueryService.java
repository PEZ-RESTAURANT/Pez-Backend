package com.pezbackend.billing.domain.services;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.queries.*;

import java.util.List;

public interface SaleQueryService {

    List<Sale> handle(GetAllSalesQuery query);

    Sale handle(GetSaleByIdQuery query);

    List<Sale> handle(GetSalesByDocumentTypeQuery query);

    List<Sale> handle(GetSalesByPaymentMethodQuery query);

    List<Sale> handle(GetSalesByStaffQuery query);

    List<Sale> handle(GetSalesBetweenDatesQuery query);

    List<Sale> handle(GetCurrentSalesQuery query);
}