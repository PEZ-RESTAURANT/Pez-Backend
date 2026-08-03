package com.pezbackend.billing.infrastructure.persistence.jpa.repositories;

import com.pezbackend.billing.domain.model.entities.PaymentMethodConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodConfigRepository extends JpaRepository<PaymentMethodConfig, Long> {
    List<PaymentMethodConfig> findAllByActiveTrue();
}
