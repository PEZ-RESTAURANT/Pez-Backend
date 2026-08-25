package com.pezbackend.staff.application.internal.queryservices;

import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import com.pezbackend.staff.domain.model.entities.Sanction;
import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import com.pezbackend.staff.domain.model.valueobjects.PayrollAdjustmentType;
import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.staff.domain.model.valueobjects.PaymentSummary;
import com.pezbackend.staff.domain.services.StaffQueryService;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.*;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de StaffQueryService encargado de consultar y resumir la información del personal.
 */
@Service
@RequiredArgsConstructor
public class StaffQueryServiceImpl implements StaffQueryService {

    private final StaffProfileRepository staffProfileRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final SanctionRepository sanctionRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StaffProfile> getAllProfiles() {
        return staffProfileRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffProfile> getProfileById(Long id) {
        return staffProfileRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffProfile> getProfileByAccountId(Long accountId) {
        return staffProfileRepository.findByAccountId(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceByProfileId(Long profileId) {
        if (!staffProfileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado.");
        }
        return attendanceRecordRepository.findAllByStaffProfileId(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getUnresolvedAttendance() {
        return attendanceRecordRepository.findAllByIsUnresolvedTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollAdjustment> getPayrollAdjustmentsByProfileId(Long profileId) {
        if (!staffProfileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado.");
        }
        return payrollAdjustmentRepository.findAllByStaffProfileId(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sanction> getSanctionsByProfileId(Long profileId) {
        if (!staffProfileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado.");
        }
        return sanctionRepository.findAllByStaffProfileId(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OvertimeRecord> getOvertimeByProfileId(Long profileId) {
        if (!staffProfileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado.");
        }
        return overtimeRecordRepository.findAllByStaffProfileId(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummary getPaymentSummary(Long profileId) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        List<PayrollAdjustment> adjustments = payrollAdjustmentRepository.findAllByStaffProfileId(profileId);
        BigDecimal totalAdvances = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        for (PayrollAdjustment adj : adjustments) {
            if (adj.getType() == PayrollAdjustmentType.ADVANCE) {
                totalAdvances = totalAdvances.add(adj.getAmount());
            } else if (adj.getType() == PayrollAdjustmentType.CONSUMPTION_DEDUCTION) {
                totalDeductions = totalDeductions.add(adj.getAmount());
            }
        }

        List<OvertimeRecord> overtimeRecords = overtimeRecordRepository.findAllByStaffProfileId(profileId);
        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        for (OvertimeRecord ov : overtimeRecords) {
            totalOvertimeHours = totalOvertimeHours.add(ov.getHours());
        }

        BigDecimal agreedAmount = profile.getAgreedAmount();
        BigDecimal regularHourlyRate = profile.getAgreedAmount();
        BigDecimal overtimeHourlyRate = profile.getOvertimeHourlyRate() != null ? profile.getOvertimeHourlyRate() : profile.getAgreedAmount();

        // Calcular horas cumplidas (regulares) a partir de AttendanceRecord
        List<AttendanceRecord> attendances = attendanceRecordRepository.findAllByStaffProfileId(profileId);
        double regularHoursSum = 0.0;
        for (AttendanceRecord record : attendances) {
            if (record.getCheckInAt() != null && record.getCheckOutAt() != null && !record.isUnresolved()) {
                long minutes = java.time.Duration.between(record.getCheckInAt(), record.getCheckOutAt()).toMinutes();
                regularHoursSum += minutes / 60.0;
            }
        }
        BigDecimal regularHours = BigDecimal.valueOf(regularHoursSum).setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal basePay;
        if (profile.getPaymentType() == StaffPaymentType.HOURLY) {
            basePay = regularHours.multiply(regularHourlyRate);
        } else {
            basePay = agreedAmount;
        }

        BigDecimal overtimePay = totalOvertimeHours.multiply(overtimeHourlyRate);
        BigDecimal netPending = basePay.add(overtimePay).subtract(totalAdvances).subtract(totalDeductions);

        return new PaymentSummary(
                profile.getAgreedAmount(),
                totalAdvances,
                totalDeductions,
                totalOvertimeHours,
                regularHourlyRate,
                regularHours,
                overtimeHourlyRate,
                basePay,
                overtimePay,
                netPending
        );
    }
}
