package com.pezbackend.billing.application.internal.commandservices;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.events.SaleMarkedPaid;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.billing.domain.model.valueobjects.SaleStatus;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.cashregister.domain.model.commands.AddSaleIncomeCommand;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.valueobjects.OrderStatus;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementación del servicio de comandos para la gestión de facturación y ventas.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SaleCommandServiceImpl implements SaleCommandService {

    private final SaleRepository saleRepository;
    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;
    private final CashRegisterCommandService cashRegisterCommandService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Long handle(CreateSaleCommand command) {
        // 1. Consultar comanda
        Order order = orderQueryService.getOrderById(command.orderId());

        // 2. Validar que comanda esté en ALL_DELIVERED
        if (order.getStatus() != OrderStatus.ALL_DELIVERED) {
            throw new BusinessRuleViolationException("ORDER_NOT_DELIVERED", 
                    "La comanda no está lista para facturación (debe estar en estado ALL_DELIVERED).");
        }

        // 3. Invocar transición en orders (issueReceipt)
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.issueReceipt(command.orderId(), executor);

        // 4. Resolver staffId y customerName
        Long staffId = userRepository.findByEmail(executor).map(u -> u.getId()).orElse(null);

        String customerName = command.customerName();
        if (customerName == null || customerName.isBlank()) {
            List<Sale> historicalSales = saleRepository.findByCustomerDocumentNumber(command.customerDocumentNumber());
            if (!historicalSales.isEmpty()) {
                customerName = historicalSales.get(0).getCustomerName();
            } else {
                customerName = "Público General";
            }
        }

        String tableName = "Comanda #" + order.getId();
        if (order.getTableId() != null) {
            tableName = restaurantTableRepository.findById(order.getTableId())
                    .map(t -> "Mesa " + t.getNumber())
                    .orElse("Comanda #" + order.getId());
        }

        // 5. Crear venta con estado ISSUED_UNPAID
        Sale sale = new Sale(
                tableName,
                staffId,
                customerName,
                command.customerDocumentNumber(),
                command.documentType(),
                command.orderId()
        );

        // 6. Clonar detalles de los platos consumidos
        order.getItems().forEach(item -> {
            String prodName = productRepository.findById(item.getProductId())
                    .map(p -> p.getName())
                    .orElse("Producto #" + item.getProductId());
            sale.addDetail(prodName, item.getUnitPriceSnapshot(), item.getQuantity(), item.getNote());
        });

        saleRepository.save(sale);
        return sale.getId();
    }

    @Override
    public void registerPayments(Long saleId, List<PaymentDetail> payments, String executor) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("SALE_NOT_FOUND", "Venta no encontrada con ID: " + saleId));

        if (sale.getSaleStatus() == SaleStatus.PAID) {
            throw new BusinessRuleViolationException("SALE_ALREADY_PAID", "La venta ya ha sido pagada en su totalidad.");
        }

        // Registrar cada pago
        for (PaymentDetail p : payments) {
            sale.addPayment(p.method(), p.amount());

            // Si es en efectivo, registrar ingreso en caja
            if (p.method() == PaymentMethod.CASH) {
                // CashRegisterCommandService valida internamente si hay una caja abierta, lanzando excepción si está cerrada
                cashRegisterCommandService.handle(new AddSaleIncomeCommand(
                        p.amount(),
                        "Ingreso por venta ID: " + sale.getId()
                ));
            }
        }

        // Calcular total pagado acumulado
        BigDecimal totalPaid = sale.getPayments().stream()
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(sale.getTotal()) > 0) {
            throw new BusinessRuleViolationException("PAYMENT_EXCEEDS_TOTAL", 
                    "La suma de pagos excede el total de la venta de " + sale.getTotal() + ".");
        }

        saleRepository.save(sale);

        // Si completa el pago
        if (totalPaid.compareTo(sale.getTotal()) == 0) {
            sale.setSaleStatus(SaleStatus.PAID);
            saleRepository.save(sale);

            // Marcar comanda como pagada en orders (libera la mesa)
            orderCommandService.markAsPaid(sale.getOrderId());

            // Publicar evento de dominio
            eventPublisher.publishEvent(new SaleMarkedPaid(sale.getId(), sale.getOrderId(), sale.getTotal()));
        }
    }
}