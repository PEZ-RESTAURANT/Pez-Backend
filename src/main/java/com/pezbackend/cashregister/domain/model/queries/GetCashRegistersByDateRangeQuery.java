package com.pezbackend.cashregister.domain.model.queries;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.time.LocalDateTime;

public record GetCashRegistersByDateRangeQuery(
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public GetCashRegistersByDateRangeQuery {
        if (startDate == null || endDate == null)
            throw new BadRequestException("Start date and end date are required");

        if (endDate.isBefore(startDate))
            throw new BadRequestException("End date cannot be before start date");
    }
}