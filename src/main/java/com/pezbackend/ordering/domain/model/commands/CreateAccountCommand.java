package com.pezbackend.ordering.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public record CreateAccountCommand(
        Long staffId
) {
    public CreateAccountCommand {
        if (staffId == null || staffId <= 0) {
            throw new BadRequestException("StaffId must be greater than zero.");
        }
    }
}