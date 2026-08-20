package com.pezbackend.billing.interfaces.rest.transform;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.entities.SaleDetail;
import com.pezbackend.billing.domain.model.entities.SalePayment;
import com.pezbackend.billing.interfaces.rest.resources.SaleDetailResource;
import com.pezbackend.billing.interfaces.rest.resources.SalePaymentResource;
import com.pezbackend.billing.interfaces.rest.resources.SaleResource;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ensamblador para convertir la entidad Sale a su recurso DTO SaleResource.
 */
@Component
public class SaleResourceFromEntityAssembler {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public SaleResourceFromEntityAssembler(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public SaleResource toResourceFromEntity(Sale sale) {
        String cashierName = "";
        if (sale.getStaffId() != null) {
            Optional<User> cashierOpt = userRepository.findById(sale.getStaffId());
            if (cashierOpt.isPresent()) {
                cashierName = cashierOpt.get().getFirstName() + " " + cashierOpt.get().getLastName();
            }
        }

        String waiterName = "";
        LocalDateTime orderCreatedAt = null;
        LocalDateTime orderDeliveredAt = null;

        if (sale.getOrderId() != null) {
            Optional<Order> orderOpt = orderRepository.findById(sale.getOrderId());
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                orderCreatedAt = order.getCreatedAt();
                orderDeliveredAt = order.getItems().stream()
                        .map(OrderItem::getDeliveredAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                if (!order.getItems().isEmpty()) {
                    Long waiterId = order.getItems().get(0).getWaiterId();
                    if (waiterId != null) {
                        Optional<User> waiterOpt = userRepository.findById(waiterId);
                        if (waiterOpt.isPresent()) {
                            waiterName = waiterOpt.get().getFirstName() + " " + waiterOpt.get().getLastName();
                        }
                    }
                }
            }
        }

        return new SaleResource(
                sale.getId(),
                sale.getName(),
                sale.getStaffId(),
                cashierName,
                waiterName,
                sale.getCustomerName(),
                sale.getCustomerDocumentNumber(),
                sale.getDocumentType(),
                sale.getSaleStatus() != null ? sale.getSaleStatus().name() : null,
                sale.getOrderId(),
                orderCreatedAt,
                orderDeliveredAt,
                sale.getTicketNumber(),
                sale.getTotal(),
                sale.getCreatedAt(),
                sale.getDetails().stream()
                        .map(this::toDetailResource)
                        .collect(Collectors.toList()),
                sale.getPayments().stream()
                        .map(this::toPaymentResource)
                        .collect(Collectors.toList()),
                sale.getVoidedReason(),
                sale.getVoidedBy(),
                sale.getVoidedAt()
        );
    }

    private SaleDetailResource toDetailResource(SaleDetail detail) {
        return new SaleDetailResource(
                detail.getProductName(),
                detail.getUnitPrice(),
                detail.getQuantity(),
                detail.getTotalPrice(),
                detail.getNote()
        );
    }

    private SalePaymentResource toPaymentResource(SalePayment payment) {
        return new SalePaymentResource(
                payment.getMethod(),
                payment.getAmount()
        );
    }
}