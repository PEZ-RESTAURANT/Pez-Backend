package com.pezbackend.cashregister.domain.model.queries;

import java.time.LocalDateTime;

public record GetCashRegistersByDateRangeQuery(
        LocalDateTime startDate,
        LocalDateTime endDate
) {}