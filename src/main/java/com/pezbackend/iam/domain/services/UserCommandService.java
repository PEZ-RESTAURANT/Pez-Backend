package com.pezbackend.iam.domain.services;

import com.pezbackend.iam.domain.model.commands.SignInCommand;
import com.pezbackend.iam.domain.model.commands.SignUpCommand;

public interface UserCommandService {
    void handle(SignUpCommand command);

    void handle(SignInCommand command);
}
