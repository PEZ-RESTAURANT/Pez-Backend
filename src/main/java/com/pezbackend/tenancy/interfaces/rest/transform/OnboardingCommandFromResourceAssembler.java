package com.pezbackend.tenancy.interfaces.rest.transform;

import com.pezbackend.tenancy.domain.model.commands.OnboardingCommand;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;

/**
 * Assembler to transform {@link OnboardingResource} into {@link OnboardingCommand}.
 */
public class OnboardingCommandFromResourceAssembler {
    public static OnboardingCommand toCommandFromResource(OnboardingResource resource) {
        return new OnboardingCommand(
                resource.name(),
                resource.businessDocumentNumber(),
                resource.contactEmail(),
                resource.contactPhone(),
                resource.adminEmail(),
                resource.adminPassword(),
                resource.adminFirstName(),
                resource.adminLastName(),
                resource.inviteCode()
        );
    }
}
