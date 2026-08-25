package com.pezbackend.staff.domain.services;

import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import com.pezbackend.staff.domain.model.entities.Sanction;
import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.staff.domain.model.valueobjects.AttendanceMethod;
import com.pezbackend.staff.domain.model.valueobjects.PayrollAdjustmentType;
import com.pezbackend.staff.domain.model.valueobjects.SanctionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servicio de dominio/aplicación para las operaciones de comando (escritura) del contexto de personal.
 */
public interface StaffCommandService {

    StaffProfile createProfile(Long accountId, StaffPaymentType paymentType, BigDecimal agreedAmount, Integer fingerprintId);
    StaffProfile createProfile(Long accountId, StaffPaymentType paymentType, BigDecimal agreedAmount, BigDecimal overtimeHourlyRate, Integer fingerprintId);

    StaffProfile updateProfile(Long profileId, StaffPaymentType paymentType, BigDecimal agreedAmount, Integer fingerprintId);
    StaffProfile updateProfile(Long profileId, StaffPaymentType paymentType, BigDecimal agreedAmount, BigDecimal overtimeHourlyRate, Integer fingerprintId);

    StaffProfile recordFingerprintConsent(Long profileId, boolean consent);

    AttendanceRecord resolveAttendance(Long recordId, LocalDateTime checkOutAt, String resolverUsername);

    AttendanceRecord checkIn(Long profileId, AttendanceMethod method, LocalDateTime checkInAt);

    AttendanceRecord checkOut(Long profileId, LocalDateTime checkOutAt);

    AttendanceRecord processFingerprintEvent(String deviceSerialNumber, Integer deviceUserId, LocalDateTime timestamp);

    PayrollAdjustment registerPayrollAdjustment(Long profileId, PayrollAdjustmentType type, BigDecimal amount, Long saleId, String registeredBy, LocalDate date);

    Sanction registerSanction(Long profileId, SanctionType type, String reason, String registeredBy, LocalDate date);

    OvertimeRecord registerOvertime(Long profileId, BigDecimal hours, LocalDate date, String registeredBy);
}
