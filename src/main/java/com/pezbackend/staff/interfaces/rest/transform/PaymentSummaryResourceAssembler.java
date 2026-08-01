package com.pezbackend.staff.interfaces.rest.transform;

import com.pezbackend.staff.domain.model.valueobjects.PaymentSummary;
import com.pezbackend.staff.interfaces.rest.resources.PaymentSummaryResource;

/**
 * Ensamblador para convertir el value object PaymentSummary a su DTO PaymentSummaryResource.
 */
public class PaymentSummaryResourceAssembler {

    public static PaymentSummaryResource toResource(PaymentSummary summary) {
        return new PaymentSummaryResource(
                summary.agreedAmount(),
                summary.totalAdvances(),
                summary.totalDeductions(),
                summary.totalOvertimeHours(),
                summary.netPending()
        );
    }
}
