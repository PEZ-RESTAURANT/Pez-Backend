package io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.transform;


import io.github.rafaviv.panther.pantherbackend.iam.domain.model.commands.SignInCommand;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandfromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }
}
