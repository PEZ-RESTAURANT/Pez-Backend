package io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.resources;

public record SignInResource(
    String email,
    String password
) {
    
}
