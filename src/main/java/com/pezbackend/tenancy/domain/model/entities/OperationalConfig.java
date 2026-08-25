package com.pezbackend.tenancy.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entidad JPA que almacena la configuración operativa general del restaurante.
 */
@Entity
@Table(name = "operational_configs")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class OperationalConfig extends AbstractTenantEntity {

    @NotNull
    @Column(nullable = false)
    private Integer cutoffHour = 3;

    @NotNull
    @Column(nullable = false)
    private Integer cutoffMinute = 0;

    @NotNull
    @Column(nullable = false)
    private Integer unattendedThresholdMinutes = 15;

    @NotNull
    @Column(nullable = false)
    private Integer waitingDishesThresholdMinutes = 30;

    @NotNull
    @Column(name = "annulment_notification_pref", nullable = false, length = 50)
    private String annulmentNotificationPref = "INSTANT"; // INSTANT or DAILY

    @NotNull
    @Column(name = "daily_summary_time", nullable = false, length = 5)
    private String dailySummaryTime = "22:00"; // HH:mm format

    @Column(name = "last_daily_summary_sent_at")
    private LocalDate lastDailySummarySentAt;

    @NotNull
    @Column(name = "unresolved_attendance_notification_pref", nullable = false, length = 50)
    private String unresolvedAttendanceNotificationPref = "BOTH"; // ADMIN, CASHIER, or BOTH

    protected OperationalConfig() {}

    public OperationalConfig(Integer cutoffHour, Integer cutoffMinute, Integer unattendedThresholdMinutes, Integer waitingDishesThresholdMinutes) {
        this.cutoffHour = cutoffHour;
        this.cutoffMinute = cutoffMinute;
        this.unattendedThresholdMinutes = unattendedThresholdMinutes;
        this.waitingDishesThresholdMinutes = waitingDishesThresholdMinutes;
        this.annulmentNotificationPref = "INSTANT";
        this.dailySummaryTime = "22:00";
        this.unresolvedAttendanceNotificationPref = "BOTH";
    }

    public OperationalConfig(Integer cutoffHour, Integer cutoffMinute, Integer unattendedThresholdMinutes, Integer waitingDishesThresholdMinutes, String annulmentNotificationPref, String dailySummaryTime) {
        this.cutoffHour = cutoffHour;
        this.cutoffMinute = cutoffMinute;
        this.unattendedThresholdMinutes = unattendedThresholdMinutes;
        this.waitingDishesThresholdMinutes = waitingDishesThresholdMinutes;
        this.annulmentNotificationPref = annulmentNotificationPref;
        this.dailySummaryTime = dailySummaryTime;
        this.unresolvedAttendanceNotificationPref = "BOTH";
    }

    public OperationalConfig(Integer cutoffHour, Integer cutoffMinute, Integer unattendedThresholdMinutes, Integer waitingDishesThresholdMinutes, String annulmentNotificationPref, String dailySummaryTime, String unresolvedAttendanceNotificationPref) {
        this.cutoffHour = cutoffHour;
        this.cutoffMinute = cutoffMinute;
        this.unattendedThresholdMinutes = unattendedThresholdMinutes;
        this.waitingDishesThresholdMinutes = waitingDishesThresholdMinutes;
        this.annulmentNotificationPref = annulmentNotificationPref;
        this.dailySummaryTime = dailySummaryTime;
        this.unresolvedAttendanceNotificationPref = unresolvedAttendanceNotificationPref;
    }
}