package com.pezbackend.billing.application.internal.queryservices;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.queries.*;
import com.pezbackend.billing.domain.services.SaleQueryService;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    public List<Sale> handle(GetCurrentSalesQuery query) {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        return saleRepository.findByCreatedAtBetween(startOfDay, now);
    }

    @Override
    public List<Sale> handle(GetSalesBetweenDatesQuery query) {

        if (query.startDate() != null && query.endDate() != null) {
            return saleRepository
                    .findByCreatedAtBetween(query.startDate(), query.endDate());
        }

        return saleRepository.findAll();
    }
}