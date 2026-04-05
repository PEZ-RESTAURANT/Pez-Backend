package com.pezbackend.billing.infrastructure.persistence.jpa.repositories;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByDocumentType(DocumentType documentType);

    @Query("""
    SELECT DISTINCT s FROM Sale s
    JOIN s.payments p
    WHERE p.method = :method
    """)
    List<Sale> findByPaymentMethod(@Param("method") PaymentMethod method);

    List<Sale> findByStaffId(Long staffId);

    List<Sale> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}