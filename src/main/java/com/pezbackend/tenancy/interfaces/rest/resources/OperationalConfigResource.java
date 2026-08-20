package com.pezbackend.tenancy.interfaces.rest.resources;

public record OperationalConfigResource(
        Integer cutoffHour,
        Integer cutoffMinute,
        Integer unattendedThresholdMinutes,
        Integer waitingDishesThresholdMinutes,
        String annulmentNotificationPref,
        String dailySummaryTime
) {}
