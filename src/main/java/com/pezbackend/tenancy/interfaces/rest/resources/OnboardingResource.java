package com.pezbackend.tenancy.interfaces.rest.resources;

/**
 * Recurso REST para recibir los datos de onboarding de un nuevo restaurante.
 */
public record OnboardingResource(
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
    public OnboardingResource(
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
}
