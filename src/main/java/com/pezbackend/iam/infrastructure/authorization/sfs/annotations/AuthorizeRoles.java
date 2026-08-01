package com.pezbackend.iam.infrastructure.authorization.sfs.annotations;

public final class AuthorizeRoles {

    private AuthorizeRoles() {}

    public static final String ADMIN = "hasRole('ADMIN')";
    public static final String CASHIER_OR_ADMIN = "hasAnyRole('ADMIN','CASHIER')";
    public static final String WAITER_OR_ADMIN = "hasAnyRole('ADMIN','WAITER')";
    public static final String COOK_OR_ADMIN = "hasAnyRole('ADMIN','COOK')";
    public static final String ANY_AUTHENTICATED = "isAuthenticated()";
}