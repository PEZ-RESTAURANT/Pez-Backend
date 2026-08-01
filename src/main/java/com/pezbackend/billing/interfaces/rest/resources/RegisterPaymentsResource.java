package com.pezbackend.billing.interfaces.rest.resources;

import java.util.List;

/**
 * DTO que representa la lista de pagos a registrar en una venta.
 */
public record RegisterPaymentsResource(
        List<PaymentDetailResource> payments
) {}
