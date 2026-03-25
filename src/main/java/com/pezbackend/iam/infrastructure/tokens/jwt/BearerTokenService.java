package io.github.rafaviv.panther.pantherbackend.iam.infrastructure.tokens.jwt;

import io.github.rafaviv.panther.pantherbackend.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;


public interface BearerTokenService extends TokenService {

    /**
     * This method is responsible for extracting the JWT token from the HTTP request.
     * @param token the HTTP request
     * @return String the JWT token
     */
    String getBearerTokenFrom(HttpServletRequest token);


}