package com.pezbackend.cashregister.domain.model.queries;

import java.util.Date;

public record GetCashRegistersByDateRangeQuery(
        Date startDate,
        Date endDate
) {}