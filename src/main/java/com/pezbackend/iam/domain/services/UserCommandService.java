package com.pezbackend.iam.domain.services;

import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.commands.SignInCommand;
import com.pezbackend.iam.domain.model.commands.SignUpCommand;
import com.pezbackend.iam.domain.model.commands.UpdateUserCommand;

public interface UserCommandService {
    User handle(SignUpCommand command);

    void handle(SignInCommand command);

    User handle(UpdateUserCommand command);
}
