package com.pezbackend.tenancy.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.tenancy.domain.model.entities.OperationalConfig;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.OperationalConfigRepository;
import com.pezbackend.tenancy.interfaces.rest.resources.OperationalConfigResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operational-configs")
@RequiredArgsConstructor
@Tag(name = "Operational Config", description = "Endpoints para la configuración de turnos, caja y tolerancias de mesas")
public class OperationalConfigController {

    private final OperationalConfigRepository operationalConfigRepository;

    @GetMapping
    @RequiresPermission("catalog.edit_schedules_thresholds")
    public ResponseEntity<OperationalConfigResource> getConfig() {
        OperationalConfig config = operationalConfigRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> new OperationalConfig(3, 0, 15, 30));
        return ResponseEntity.ok(new OperationalConfigResource(
                config.getCutoffHour(),
                config.getCutoffMinute(),
                config.getUnattendedThresholdMinutes(),
                config.getWaitingDishesThresholdMinutes(),
                config.getAnnulmentNotificationPref(),
                config.getDailySummaryTime(),
                config.getUnresolvedAttendanceNotificationPref()
        ));
    }

    @PutMapping
    @RequiresPermission("catalog.edit_schedules_thresholds")
    public ResponseEntity<OperationalConfigResource> updateConfig(@RequestBody OperationalConfigResource resource) {
        List<OperationalConfig> configs = operationalConfigRepository.findAll();
        OperationalConfig config;
        if (configs.isEmpty()) {
            config = new OperationalConfig(
                    resource.cutoffHour(),
                    resource.cutoffMinute(),
                    resource.unattendedThresholdMinutes(),
                    resource.waitingDishesThresholdMinutes(),
                    resource.annulmentNotificationPref() != null ? resource.annulmentNotificationPref() : "INSTANT",
                    resource.dailySummaryTime() != null ? resource.dailySummaryTime() : "22:00",
                    resource.unresolvedAttendanceNotificationPref() != null ? resource.unresolvedAttendanceNotificationPref() : "BOTH"
            );
        } else {
            config = configs.get(0);
            config.setCutoffHour(resource.cutoffHour());
            config.setCutoffMinute(resource.cutoffMinute());
            config.setUnattendedThresholdMinutes(resource.unattendedThresholdMinutes());
            config.setWaitingDishesThresholdMinutes(resource.waitingDishesThresholdMinutes());
            if (resource.annulmentNotificationPref() != null) {
                config.setAnnulmentNotificationPref(resource.annulmentNotificationPref());
            }
            if (resource.dailySummaryTime() != null) {
                config.setDailySummaryTime(resource.dailySummaryTime());
            }
            if (resource.unresolvedAttendanceNotificationPref() != null) {
                config.setUnresolvedAttendanceNotificationPref(resource.unresolvedAttendanceNotificationPref());
            }
        }
        config = operationalConfigRepository.save(config);
        return ResponseEntity.ok(new OperationalConfigResource(
                config.getCutoffHour(),
                config.getCutoffMinute(),
                config.getUnattendedThresholdMinutes(),
                config.getWaitingDishesThresholdMinutes(),
                config.getAnnulmentNotificationPref(),
                config.getDailySummaryTime(),
                config.getUnresolvedAttendanceNotificationPref()
        ));
    }
}
