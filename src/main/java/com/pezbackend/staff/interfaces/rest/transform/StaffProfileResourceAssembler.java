package com.pezbackend.staff.interfaces.rest.transform;

import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.interfaces.rest.resources.StaffProfileResource;

/**
 * Ensamblador para convertir la entidad StaffProfile a su DTO StaffProfileResource.
 */
public class StaffProfileResourceAssembler {

    public static StaffProfileResource toResource(StaffProfile profile) {
        return new StaffProfileResource(
                profile.getId(),
                profile.getAccountId(),
                profile.getPaymentType().name(),
                profile.getAgreedAmount(),
                profile.getOvertimeHourlyRate(),
                profile.isFingerprintConsent(),
                profile.getFingerprintConsentDate() != null ? profile.getFingerprintConsentDate().toString() : null,
                profile.getFingerprintId()
        );
    }
}
