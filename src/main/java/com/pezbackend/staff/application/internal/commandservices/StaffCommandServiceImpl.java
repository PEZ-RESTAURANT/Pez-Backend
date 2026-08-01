package com.pezbackend.staff.application.internal.commandservices;

import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import com.pezbackend.staff.domain.model.entities.Sanction;
import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.staff.domain.model.valueobjects.AttendanceMethod;
import com.pezbackend.staff.domain.model.valueobjects.PayrollAdjustmentType;
import com.pezbackend.staff.domain.model.valueobjects.SanctionType;
import com.pezbackend.staff.domain.model.events.AttendanceRecorded;
import com.pezbackend.staff.domain.model.events.PayrollAdjustmentRegistered;
import com.pezbackend.staff.domain.model.events.SanctionRegistered;
import com.pezbackend.staff.domain.model.events.OvertimeRegistered;
import com.pezbackend.staff.domain.services.StaffCommandService;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.*;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementación de StaffCommandService encargado de ejecutar la lógica de negocio de comandos
 * en el contexto de personal.
 */
@Service
@RequiredArgsConstructor
public class StaffCommandServiceImpl implements StaffCommandService {

    private final StaffProfileRepository staffProfileRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final SanctionRepository sanctionRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public StaffProfile createProfile(Long accountId, StaffPaymentType paymentType, BigDecimal agreedAmount) {
        if (!userRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("USER_NOT_FOUND", "La cuenta de usuario con ID " + accountId + " no existe.");
        }

        if (staffProfileRepository.findByAccountId(accountId).isPresent()) {
            throw new BusinessRuleViolationException("PROFILE_ALREADY_EXISTS", "El perfil para esta cuenta de usuario ya existe.");
        }

        if (agreedAmount == null || agreedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_AMOUNT", "El monto acordado debe ser igual o mayor a cero.");
        }

        StaffProfile profile = new StaffProfile(accountId, paymentType, agreedAmount);
        return staffProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public StaffProfile updateProfile(Long profileId, StaffPaymentType paymentType, BigDecimal agreedAmount) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        if (agreedAmount == null || agreedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_AMOUNT", "El monto acordado debe ser igual o mayor a cero.");
        }

        profile.updateProfile(paymentType, agreedAmount);
        return staffProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public StaffProfile recordFingerprintConsent(Long profileId, boolean consent) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        profile.recordFingerprintConsent(consent);
        return staffProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public AttendanceRecord checkIn(Long profileId, AttendanceMethod method, LocalDateTime checkInAt) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        if (method == AttendanceMethod.FINGERPRINT_HASH && !profile.isFingerprintConsent()) {
            throw new BusinessRuleViolationException("FINGERPRINT_CONSENT_REQUIRED", "El empleado no ha dado consentimiento para el registro por huella dactilar.");
        }

        if (attendanceRecordRepository.findFirstByStaffProfileIdAndCheckOutAtIsNullOrderByCheckInAtDesc(profileId).isPresent()) {
            throw new BusinessRuleViolationException("ACTIVE_CHECK_IN_EXISTS", "El empleado ya posee una marca de entrada activa (sin salida).");
        }

        AttendanceRecord record = new AttendanceRecord(profileId, checkInAt, method);
        record = attendanceRecordRepository.save(record);

        eventPublisher.publishEvent(new AttendanceRecorded(profileId, record.getId(), method.name(), true));
        return record;
    }

    @Override
    @Transactional
    public AttendanceRecord checkOut(Long profileId, LocalDateTime checkOutAt) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        AttendanceRecord record = attendanceRecordRepository.findFirstByStaffProfileIdAndCheckOutAtIsNullOrderByCheckInAtDesc(profileId)
                .orElseThrow(() -> new BusinessRuleViolationException("NO_ACTIVE_CHECK_IN", "El empleado no posee una marca de entrada activa."));

        if (checkOutAt.isBefore(record.getCheckInAt())) {
            throw new BusinessRuleViolationException("INVALID_CHECK_OUT_TIME", "La hora de salida no puede ser anterior a la hora de entrada.");
        }

        record.recordCheckOut(checkOutAt);
        record = attendanceRecordRepository.save(record);

        eventPublisher.publishEvent(new AttendanceRecorded(profileId, record.getId(), record.getMethod().name(), false));
        return record;
    }

    @Override
    @Transactional
    public PayrollAdjustment registerPayrollAdjustment(Long profileId, PayrollAdjustmentType type, BigDecimal amount, Long saleId, String registeredBy, LocalDate date) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("INVALID_AMOUNT", "El monto del ajuste debe ser mayor a cero.");
        }

        if (type == PayrollAdjustmentType.CONSUMPTION_DEDUCTION && saleId != null) {
            if (!saleRepository.existsById(saleId)) {
                throw new ResourceNotFoundException("SALE_NOT_FOUND", "El comprobante de venta asociado con ID " + saleId + " no existe.");
            }
        }

        PayrollAdjustment adjustment = new PayrollAdjustment(profileId, type, amount, saleId, registeredBy, date);
        adjustment = payrollAdjustmentRepository.save(adjustment);

        eventPublisher.publishEvent(new PayrollAdjustmentRegistered(profileId, adjustment.getId(), type.name(), amount, registeredBy));
        return adjustment;
    }

    @Override
    @Transactional
    public Sanction registerSanction(Long profileId, SanctionType type, String reason, String registeredBy, LocalDate date) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_REASON", "El motivo de la sanción es obligatorio.");
        }

        Sanction sanction = new Sanction(profileId, type, reason, registeredBy, date);
        sanction = sanctionRepository.save(sanction);

        eventPublisher.publishEvent(new SanctionRegistered(profileId, sanction.getId(), type.name(), registeredBy));
        return sanction;
    }

    @Override
    @Transactional
    public OvertimeRecord registerOvertime(Long profileId, BigDecimal hours, LocalDate date, String registeredBy) {
        StaffProfile profile = staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("STAFF_PROFILE_NOT_FOUND", "Perfil de personal no encontrado."));

        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("INVALID_HOURS", "Las horas extras deben ser mayores a cero.");
        }

        OvertimeRecord overtime = new OvertimeRecord(profileId, hours, date, registeredBy);
        overtime = overtimeRecordRepository.save(overtime);

        eventPublisher.publishEvent(new OvertimeRegistered(profileId, overtime.getId(), hours, registeredBy));
        return overtime;
    }
}
