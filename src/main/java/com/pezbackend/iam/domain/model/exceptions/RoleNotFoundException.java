package com.pezbackend.iam.domain.model.exceptions;

import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.shared.domain.model.exceptions.NotFoundException;

public class RoleNotFoundException extends NotFoundException {
    public RoleNotFoundException(Roles requestedRole) {
        super("Role " + requestedRole + " not found");
    }
}
