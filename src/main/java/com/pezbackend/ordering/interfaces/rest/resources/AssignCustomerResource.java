package com.pezbackend.ordering.interfaces.rest.resources;

public record AssignCustomerResource(
        String customerName,
        String dni,
        String ruc
) {}