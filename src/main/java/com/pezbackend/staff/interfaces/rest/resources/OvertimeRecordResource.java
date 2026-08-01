package com.pezbackend.staff.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para representar el registro de horas extras.
 */
public record OvertimeRecordResource(
        Long id,
        Long staffProfileId,
        BigDecimal hours,
        String date,
        String registeredBy
) {}
