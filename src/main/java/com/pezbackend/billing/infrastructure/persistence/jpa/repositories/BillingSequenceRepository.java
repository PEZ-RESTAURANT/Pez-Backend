package com.pezbackend.billing.infrastructure.persistence.jpa.repositories;

import com.pezbackend.billing.domain.model.entities.BillingSequence;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingSequenceRepository extends JpaRepository<BillingSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BillingSequence s WHERE s.restaurantId = :restaurantId AND s.documentType = :documentType")
    Optional<BillingSequence> findByRestaurantIdAndDocumentTypeForUpdate(
            @Param("restaurantId") Long restaurantId,
            @Param("documentType") DocumentType documentType
    );
}
