package com.pezbackend.billing.infrastructure.persistence.jpa.repositories;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByDocumentType(DocumentType documentType);

    List<Sale> findByPaymentMethod(PaymentMethod paymentMethod);

    List<Sale> findByStaffId(Long staffId);
}