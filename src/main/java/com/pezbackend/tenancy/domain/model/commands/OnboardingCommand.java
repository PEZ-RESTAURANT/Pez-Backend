package com.pezbackend.tenancy.domain.model.commands;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

/**
 * Comando para realizar el onboarding de un nuevo restaurante junto con su usuario administrador.
 */
public record OnboardingCommand(
        String name,
        String businessDocumentNumber,
        String contactEmail,
        String contactPhone,
        String adminEmail,
        String adminPassword,
        String adminFirstName,
        String adminLastName,
        String inviteCode
) {
    public OnboardingCommand(
            String name,
            String businessDocumentNumber,
            String contactEmail,
            String contactPhone,
            String adminEmail,
            String adminPassword,
            String adminFirstName,
            String adminLastName
    ) {
        this(name, businessDocumentNumber, contactEmail, contactPhone, adminEmail, adminPassword, adminFirstName, adminLastName, "TEST-INVITE-CODE");
    }

    public OnboardingCommand {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Restaurant name cannot be empty.");
        }
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new BadRequestException("Administrator email cannot be empty.");
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new BadRequestException("Administrator password cannot be empty.");
        }
        if (adminFirstName == null || adminFirstName.isBlank()) {
            throw new BadRequestException("Administrator first name cannot be empty.");
        }
        if (adminLastName == null || adminLastName.isBlank()) {
            throw new BadRequestException("Administrator last name cannot be empty.");
        }
    }
}
