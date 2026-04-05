package com.pezbackend.ordering.interfaces.rest.resources;

import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AccountResource(
        Long id,
        String name,
        AccountStatus status,
        Long staffId,

        String customerName,
        String customerDni,
        String customerRuc,

        BigDecimal total,

        List<AccountItemResource> items,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}