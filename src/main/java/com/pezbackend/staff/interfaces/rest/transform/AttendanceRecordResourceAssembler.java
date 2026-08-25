package com.pezbackend.staff.interfaces.rest.transform;

import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.interfaces.rest.resources.AttendanceRecordResource;

/**
 * Ensamblador para convertir la entidad AttendanceRecord a su DTO AttendanceRecordResource.
 */
public class AttendanceRecordResourceAssembler {

    public static AttendanceRecordResource toResource(AttendanceRecord record) {
        return new AttendanceRecordResource(
                record.getId(),
                record.getStaffProfileId(),
                record.getCheckInAt().toString(),
                record.getCheckOutAt() != null ? record.getCheckOutAt().toString() : null,
                record.getMethod().name(),
                record.isUnresolved()
        );
    }
}
