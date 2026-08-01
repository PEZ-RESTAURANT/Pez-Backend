package com.pezbackend.loyalty.application.internal.eventhandlers;

import com.pezbackend.billing.domain.model.events.SaleMarkedPaid;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;
import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.domain.model.valueobjects.PointsTransactionType;
import com.pezbackend.loyalty.domain.model.events.PointsEarned;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.CustomerRepository;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.PointsTransactionRepository;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.LoyaltyConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Event handler encargado de escuchar los eventos de facturación para otorgar puntos de fidelización de forma asíncrona/desacoplada.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoyaltyEventListener {

    private final OrderQueryService orderQueryService;
    private final CustomerRepository customerRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final LoyaltyConfigRepository loyaltyConfigRepository;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSaleMarkedPaid(SaleMarkedPaid event) {
        log.info("Loyalty: Capturado evento SaleMarkedPaid para venta ID: {}", event.saleId());

        if (event.orderId() == null) {
            return;
        }

        try {
            Order order = orderQueryService.getOrderById(event.orderId());
            if (order == null || order.getCustomerId() == null) {
                return;
            }

            Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
            if (customer == null) {
                return;
            }

            LoyaltyConfig config = loyaltyConfigRepository.findAll().stream().findFirst().orElse(null);
            if (config == null) {
                return;
            }

            BigDecimal saleAmount = event.amount() != null ? event.amount() : BigDecimal.ZERO;
            if (saleAmount.compareTo(config.getMinPurchaseAmountForPoints()) >= 0) {
                int points = saleAmount.multiply(config.getPointsPerCurrencyUnit()).intValue();
                if (points > 0) {
                    customer.earnPoints(points);
                    customerRepository.save(customer);

                    PointsTransaction txn = new PointsTransaction(
                            customer.getId(),
                            PointsTransactionType.EARNED,
                            points,
                            event.saleId(),
                            LocalDate.now()
                    );
                    pointsTransactionRepository.save(txn);

                    eventPublisher.publishEvent(new PointsEarned(customer.getId(), points, event.saleId()));
                    log.info("Loyalty: Acreditados {} puntos al cliente ID: {} por venta ID: {}", points, customer.getId(), event.saleId());
                }
            }
        } catch (Exception e) {
            log.error("Error al procesar acumulación de puntos para orden ID: {}", event.orderId(), e);
        }
    }
}
