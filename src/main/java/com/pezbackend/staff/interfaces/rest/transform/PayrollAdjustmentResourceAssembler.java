package com.pezbackend.staff.interfaces.rest.transform;

import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import com.pezbackend.staff.interfaces.rest.resources.PayrollAdjustmentResource;

/**
 * Ensamblador para convertir la entidad PayrollAdjustment a su DTO PayrollAdjustmentResource.
 */
public class PayrollAdjustmentResourceAssembler {

    public static PayrollAdjustmentResource toResource(PayrollAdjustment adjustment) {
        return new PayrollAdjustmentResource(
                adjustment.getId(),
                adjustment.getStaffProfileId(),
                adjustment.getType().name(),
                adjustment.getAmount(),
                adjustment.getSaleId(),
                adjustment.getRegisteredBy(),
                adjustment.getDate().toString()
        );
    }
}
