package com.pezbackend.staff.interfaces.rest.transform;

import com.pezbackend.staff.domain.model.entities.Sanction;
import com.pezbackend.staff.interfaces.rest.resources.SanctionResource;

/**
 * Ensamblador para convertir la entidad Sanction a su DTO SanctionResource.
 */
public class SanctionResourceAssembler {

    public static SanctionResource toResource(Sanction sanction) {
        return new SanctionResource(
                sanction.getId(),
                sanction.getStaffProfileId(),
                sanction.getType().name(),
                sanction.getReason(),
                sanction.getRegisteredBy(),
                sanction.getDate().toString()
        );
    }
}
