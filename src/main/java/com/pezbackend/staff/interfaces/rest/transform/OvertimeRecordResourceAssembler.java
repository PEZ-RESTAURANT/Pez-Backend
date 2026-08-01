package com.pezbackend.staff.interfaces.rest.transform;

import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import com.pezbackend.staff.interfaces.rest.resources.OvertimeRecordResource;

/**
 * Ensamblador para convertir la entidad OvertimeRecord a su DTO OvertimeRecordResource.
 */
public class OvertimeRecordResourceAssembler {

    public static OvertimeRecordResource toResource(OvertimeRecord overtime) {
        return new OvertimeRecordResource(
                overtime.getId(),
                overtime.getStaffProfileId(),
                overtime.getHours(),
                overtime.getDate().toString(),
                overtime.getRegisteredBy()
        );
    }
}
