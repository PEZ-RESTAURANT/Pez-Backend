package com.pezbackend.loyalty.application.internal.queryservices;

import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;
import com.pezbackend.loyalty.domain.services.LoyaltyQueryService;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.CustomerRepository;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.PointsTransactionRepository;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.LoyaltyConfigRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de LoyaltyQueryService encargada de gestionar las consultas de fidelización.
 */
@Service
@RequiredArgsConstructor
public class LoyaltyQueryServiceImpl implements LoyaltyQueryService {

    private final CustomerRepository customerRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final LoyaltyConfigRepository loyaltyConfigRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsTransaction> getPointsHistory(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Cliente no encontrado.");
        }
        return pointsTransactionRepository.findAllByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public LoyaltyConfig getConfig() {
        return loyaltyConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> new LoyaltyConfig(BigDecimal.TEN, BigDecimal.ONE, 4, "https://google.com/review"));
    }
}
