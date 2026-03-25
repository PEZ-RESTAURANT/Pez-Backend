package com.pezbackend.iam.interfaces.rest.transform;


import com.pezbackend.iam.domain.model.commands.SignInCommand;
import com.pezbackend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandfromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }
}
