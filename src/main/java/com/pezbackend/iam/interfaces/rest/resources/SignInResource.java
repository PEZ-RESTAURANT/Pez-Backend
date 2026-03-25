package com.pezbackend.iam.interfaces.rest.resources;

public record SignInResource(
    String email,
    String password
) {
    
}
