package com.pezbackend.staff.domain.services;

import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import com.pezbackend.staff.domain.model.entities.Sanction;
import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import com.pezbackend.staff.domain.model.valueobjects.PaymentSummary;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio/aplicación para las operaciones de consulta (lectura) del contexto de personal.
 */
public interface StaffQueryService {

    List<StaffProfile> getAllProfiles();

    Optional<StaffProfile> getProfileById(Long id);

    Optional<StaffProfile> getProfileByAccountId(Long accountId);

    List<AttendanceRecord> getAttendanceByProfileId(Long profileId);

    List<AttendanceRecord> getUnresolvedAttendance();

    List<PayrollAdjustment> getPayrollAdjustmentsByProfileId(Long profileId);

    List<Sanction> getSanctionsByProfileId(Long profileId);

    List<OvertimeRecord> getOvertimeByProfileId(Long profileId);

    PaymentSummary getPaymentSummary(Long profileId);
}
