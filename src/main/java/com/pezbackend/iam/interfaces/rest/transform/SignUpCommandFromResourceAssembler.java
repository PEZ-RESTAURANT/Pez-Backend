package io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.transform;

import io.github.rafaviv.panther.pantherbackend.iam.domain.model.commands.SignUpCommand;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.resources.SignUpResource;

/**
 * Assembler for converting SignUpResource to SignUpCommand
 * <p>
 * This class provides static methods to transform data between the interface layer
 * (REST resources) and the domain layer (commands) following DDD principles.
 * </p>
 */
public class SignUpCommandFromResourceAssembler {
    
    /**
     * Converts a SignUpResource to a SignUpCommand
     * @param resource the SignUpResource from the REST request
     * @return SignUpCommand for domain processing
     */
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(
            resource.email(),
            resource.password(),
            resource.firstName(),
            resource.lastName(),
            resource.requestedRole()
        );
    }
}
