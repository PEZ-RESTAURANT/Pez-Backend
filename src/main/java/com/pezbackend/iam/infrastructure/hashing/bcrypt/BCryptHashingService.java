package io.github.rafaviv.panther.pantherbackend.iam.infrastructure.hashing.bcrypt;

import io.github.rafaviv.panther.pantherbackend.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
