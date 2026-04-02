package com.pezbackend.billing.interfaces.rest.transform;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.entities.SaleDetail;
import com.pezbackend.billing.interfaces.rest.resources.SaleDetailResource;
import com.pezbackend.billing.interfaces.rest.resources.SaleResource;

import java.util.stream.Collectors;

public class SaleResourceFromEntityAssembler {

    public static SaleResource toResourceFromEntity(Sale sale) {
        return new SaleResource(
                sale.getId(),
                sale.getName(),
                sale.getStaffId(),
                sale.getCustomerName(),
                sale.getCustomerDni(),
                sale.getCustomerRuc(),
                sale.getDocumentType(),
                sale.getPaymentMethod(),
                sale.getTotal(),
                sale.getCreatedAt(),
                sale.getUpdatedAt(),
                sale.getDetails().stream()
                        .map(SaleResourceFromEntityAssembler::toDetailResource)
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
}