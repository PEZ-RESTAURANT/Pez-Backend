package com.pezbackend.billing.application.internal.queryservices;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.queries.*;
import com.pezbackend.billing.domain.services.SaleQueryService;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleQueryServiceImpl implements SaleQueryService {

    private final SaleRepository saleRepository;

    public SaleQueryServiceImpl(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Override
    public List<Sale> handle(GetAllSalesQuery query) {
        return saleRepository.findAll();
    }

    @Override
    public Sale handle(GetSaleByIdQuery query) {
        return saleRepository.findById(query.id())
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Override
    public List<Sale> handle(GetSalesByDocumentTypeQuery query) {
        return saleRepository.findByDocumentType(query.documentType());
    }

    @Override
    public List<Sale> handle(GetSalesByPaymentMethodQuery query) {
        return saleRepository.findByPaymentMethod(query.paymentMethod());
    }

    @Override
    public List<Sale> handle(GetSalesByStaffQuery query) {
        return saleRepository.findByStaffId(query.staffId());
    }
}