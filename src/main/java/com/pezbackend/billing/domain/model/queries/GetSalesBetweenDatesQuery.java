package com.pezbackend.billing.domain.model.queries;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

import java.time.LocalDateTime;

public record GetSalesBetweenDatesQuery(
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public GetSalesBetweenDatesQuery {
        if (startDate == null || endDate == null)
            throw new BadRequestException("Start and end dates are required");

        if (endDate.isBefore(startDate))
            throw new BadRequestException("End date cannot be before start date");
    }
}
