package com.pezbackend.ordering.domain.model.commands;

public record CreateAccountCommand(
        Long staffId
) {
    public CreateAccountCommand {
        if (staffId == null || staffId <= 0) {
            throw new IllegalArgumentException("StaffId must be greater than zero.");
        }
    }
}