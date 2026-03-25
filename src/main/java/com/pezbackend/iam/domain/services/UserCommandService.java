package io.github.rafaviv.panther.pantherbackend.iam.domain.services;

import io.github.rafaviv.panther.pantherbackend.iam.domain.model.commands.SignInCommand;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.commands.SignUpCommand;

public interface UserCommandService {
    void handle(SignUpCommand command);

    void handle(SignInCommand command);
}
