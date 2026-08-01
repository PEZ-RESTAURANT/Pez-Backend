package com.pezbackend.billing.interfaces.rest.transform;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.entities.SaleDetail;
import com.pezbackend.billing.domain.model.entities.SalePayment;
import com.pezbackend.billing.interfaces.rest.resources.SaleDetailResource;
import com.pezbackend.billing.interfaces.rest.resources.SalePaymentResource;
import com.pezbackend.billing.interfaces.rest.resources.SaleResource;
import java.util.stream.Collectors;

/**
 * Ensamblador para convertir la entidad Sale a su recurso DTO SaleResource.
 */
public class SaleResourceFromEntityAssembler {

    public static SaleResource toResourceFromEntity(Sale sale) {
        return new SaleResource(
                sale.getId(),
                sale.getName(),
                sale.getStaffId(),
                sale.getCustomerName(),
                sale.getCustomerDocumentNumber(),
                sale.getDocumentType(),
                sale.getSaleStatus() != null ? sale.getSaleStatus().name() : null,
                sale.getOrderId(),
                sale.getTotal(),
                sale.getCreatedAt(),
                sale.getDetails().stream()
                        .map(SaleResourceFromEntityAssembler::toDetailResource)
                        .collect(Collectors.toList()),
                sale.getPayments().stream()
                        .map(SaleResourceFromEntityAssembler::toPaymentResource)
                        .collect(Collectors.toList())
        );
    }

    private static SaleDetailResource toDetailResource(SaleDetail detail) {
        return new SaleDetailResource(
                detail.getProductName(),
                detail.getUnitPrice(),
                detail.getQuantity(),
                detail.getTotalPrice(),
                detail.getNote()
        );
    }

    private static SalePaymentResource toPaymentResource(SalePayment payment) {
        return new SalePaymentResource(
                payment.getMethod(),
                payment.getAmount()
        );
    }
}