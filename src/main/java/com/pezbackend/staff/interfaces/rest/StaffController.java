package com.pezbackend.staff.interfaces.rest;

import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import com.pezbackend.staff.domain.model.entities.Sanction;
import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.staff.domain.model.valueobjects.AttendanceMethod;
import com.pezbackend.staff.domain.model.valueobjects.PayrollAdjustmentType;
import com.pezbackend.staff.domain.model.valueobjects.SanctionType;
import com.pezbackend.staff.domain.model.valueobjects.PaymentSummary;
import com.pezbackend.staff.domain.services.StaffCommandService;
import com.pezbackend.staff.domain.services.StaffQueryService;
import com.pezbackend.staff.interfaces.rest.resources.*;
import com.pezbackend.staff.interfaces.rest.transform.*;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST que expone los endpoints de gestión de personal.
 * Cada endpoint está protegido con el correspondiente código de permiso.
 */
@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffCommandService commandService;
    private final StaffQueryService queryService;

    private String getCurrentUserEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/profiles")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<List<StaffProfileResource>> getAllProfiles() {
        List<StaffProfileResource> resources = queryService.getAllProfiles().stream()
                .map(StaffProfileResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/profiles")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<StaffProfileResource> createProfile(@RequestBody CreateStaffProfileResource resource) {
        StaffProfile profile = commandService.createProfile(
                resource.accountId(),
                StaffPaymentType.valueOf(resource.paymentType()),
                resource.agreedAmount(),
                resource.overtimeHourlyRate(),
                resource.fingerprintId()
        );
        return ResponseEntity.ok(StaffProfileResourceAssembler.toResource(profile));
    }

    @PutMapping("/profiles/{id}")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<StaffProfileResource> updateProfilePath(@PathVariable Long id, @RequestBody CreateStaffProfileResource resource) {
        StaffProfile profile = commandService.updateProfile(
                id,
                StaffPaymentType.valueOf(resource.paymentType()),
                resource.agreedAmount(),
                resource.overtimeHourlyRate(),
                resource.fingerprintId()
        );
        return ResponseEntity.ok(StaffProfileResourceAssembler.toResource(profile));
    }

    @PutMapping("/profiles")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<StaffProfileResource> updateProfileBody(@RequestBody StaffProfileResource resource) {
        StaffProfile profile = commandService.updateProfile(
                resource.id(),
                StaffPaymentType.valueOf(resource.paymentType()),
                resource.agreedAmount(),
                resource.overtimeHourlyRate(),
                resource.fingerprintId()
        );
        return ResponseEntity.ok(StaffProfileResourceAssembler.toResource(profile));
    }

    @PostMapping("/profiles/{id}/fingerprint-consent")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<StaffProfileResource> recordFingerprintConsent(@PathVariable Long id, @RequestBody FingerprintConsentResource resource) {
        StaffProfile profile = commandService.recordFingerprintConsent(id, resource.consent());
        return ResponseEntity.ok(StaffProfileResourceAssembler.toResource(profile));
    }

    private LocalDateTime parseLocalDateTime(String input) {
        if (input == null || input.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(input);
        } catch (Exception e) {
            try {
                return java.time.OffsetDateTime.parse(input).toLocalDateTime();
            } catch (Exception ex) {
                return java.time.LocalDate.parse(input).atStartOfDay();
            }
        }
    }

    @PostMapping("/attendance/check-in")
    @RequiresPermission("staff.register_attendance")
    public ResponseEntity<AttendanceRecordResource> checkIn(@RequestBody AttendanceCheckInResource resource) {
        LocalDateTime checkInTime = parseLocalDateTime(resource.checkInAt());

        AttendanceRecord record = commandService.checkIn(
                resource.staffProfileId(),
                AttendanceMethod.valueOf(resource.method()),
                checkInTime
        );
        return ResponseEntity.ok(AttendanceRecordResourceAssembler.toResource(record));
    }

    @PostMapping("/attendance/check-out")
    @RequiresPermission("staff.register_attendance")
    public ResponseEntity<AttendanceRecordResource> checkOut(@RequestBody AttendanceCheckOutResource resource) {
        LocalDateTime checkOutTime = parseLocalDateTime(resource.checkOutAt());

        AttendanceRecord record = commandService.checkOut(
                resource.staffProfileId(),
                checkOutTime
        );
        return ResponseEntity.ok(AttendanceRecordResourceAssembler.toResource(record));
    }

    @PostMapping("/attendance/fingerprint-event")
    @RequiresPermission("staff.register_attendance")
    public ResponseEntity<AttendanceRecordResource> recordFingerprintEvent(@RequestBody FingerprintAttendanceEventResource resource) {
        LocalDateTime timestamp = parseLocalDateTime(resource.timestamp());
        AttendanceRecord record = commandService.processFingerprintEvent(
                resource.deviceSerialNumber(),
                resource.deviceUserId(),
                timestamp
        );
        if (record == null) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.ok(AttendanceRecordResourceAssembler.toResource(record));
    }

    @GetMapping("/profiles/{id}/attendance")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<List<AttendanceRecordResource>> getAttendanceByProfileId(@PathVariable Long id) {
        List<AttendanceRecordResource> resources = queryService.getAttendanceByProfileId(id).stream()
                .map(AttendanceRecordResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/profiles/{id}/payroll-adjustments")
    @RequiresPermission("staff.register_advance")
    public ResponseEntity<PayrollAdjustmentResource> registerPayrollAdjustment(@PathVariable Long id, @RequestBody RegisterPayrollAdjustmentResource resource) {
        LocalDate date = (resource.date() == null || resource.date().isBlank())
                ? LocalDate.now() : LocalDate.parse(resource.date());

        PayrollAdjustment adjustment = commandService.registerPayrollAdjustment(
                id,
                PayrollAdjustmentType.valueOf(resource.type()),
                resource.amount(),
                resource.saleId(),
                getCurrentUserEmail(),
                date
        );
        return ResponseEntity.ok(PayrollAdjustmentResourceAssembler.toResource(adjustment));
    }

    @GetMapping("/profiles/{id}/payroll-adjustments")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<List<PayrollAdjustmentResource>> getPayrollAdjustmentsByProfileId(@PathVariable Long id) {
        List<PayrollAdjustmentResource> resources = queryService.getPayrollAdjustmentsByProfileId(id).stream()
                .map(PayrollAdjustmentResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/profiles/{id}/sanctions")
    @RequiresPermission("staff.register_sanction")
    public ResponseEntity<SanctionResource> registerSanction(@PathVariable Long id, @RequestBody RegisterSanctionResource resource) {
        LocalDate date = (resource.date() == null || resource.date().isBlank())
                ? LocalDate.now() : LocalDate.parse(resource.date());

        Sanction sanction = commandService.registerSanction(
                id,
                SanctionType.valueOf(resource.type()),
                resource.reason(),
                getCurrentUserEmail(),
                date
        );
        return ResponseEntity.ok(SanctionResourceAssembler.toResource(sanction));
    }

    @GetMapping("/profiles/{id}/sanctions")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<List<SanctionResource>> getSanctionsByProfileId(@PathVariable Long id) {
        List<SanctionResource> resources = queryService.getSanctionsByProfileId(id).stream()
                .map(SanctionResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/profiles/{id}/overtime")
    @RequiresPermission("staff.register_overtime")
    public ResponseEntity<OvertimeRecordResource> registerOvertime(@PathVariable Long id, @RequestBody RegisterOvertimeResource resource) {
        LocalDate date = (resource.date() == null || resource.date().isBlank())
                ? LocalDate.now() : LocalDate.parse(resource.date());

        OvertimeRecord overtime = commandService.registerOvertime(
                id,
                resource.hours(),
                date,
                getCurrentUserEmail()
        );
        return ResponseEntity.ok(OvertimeRecordResourceAssembler.toResource(overtime));
    }

    @GetMapping("/profiles/{id}/overtime")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<List<OvertimeRecordResource>> getOvertimeByProfileId(@PathVariable Long id) {
        List<OvertimeRecordResource> resources = queryService.getOvertimeByProfileId(id).stream()
                .map(OvertimeRecordResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/profiles/{id}/payment-summary")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<PaymentSummaryResource> getPaymentSummary(@PathVariable Long id) {
        PaymentSummary summary = queryService.getPaymentSummary(id);
        return ResponseEntity.ok(PaymentSummaryResourceAssembler.toResource(summary));
    }

    @GetMapping("/attendance/unresolved")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<List<AttendanceRecordResource>> getUnresolvedAttendance() {
        List<AttendanceRecordResource> resources = queryService.getUnresolvedAttendance().stream()
                .map(AttendanceRecordResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/attendance/{id}/resolve")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<AttendanceRecordResource> resolveAttendance(
            @PathVariable Long id,
            @RequestBody ResolveAttendanceResource resource
    ) {
        LocalDateTime checkOutTime = parseLocalDateTime(resource.checkOutAt());
        AttendanceRecord resolved = commandService.resolveAttendance(id, checkOutTime, getCurrentUserEmail());
        return ResponseEntity.ok(AttendanceRecordResourceAssembler.toResource(resolved));
    }
}
