package com.pezbackend.billing.domain.model.queries;

import java.time.LocalDateTime;

public record GetSalesBetweenDatesQuery(
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
