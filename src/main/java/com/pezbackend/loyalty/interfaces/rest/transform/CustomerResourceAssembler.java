package com.pezbackend.loyalty.interfaces.rest.transform;

import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.interfaces.rest.resources.CustomerResource;

/**
 * Ensamblador para convertir la entidad Customer a su DTO CustomerResource.
 */
public class CustomerResourceAssembler {

    public static CustomerResource toResource(Customer customer) {
        return new CustomerResource(
                customer.getId(),
                customer.getPhone(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getBirthday() != null ? customer.getBirthday().toString() : null,
                customer.getAddress(),
                customer.getDocumentNumber(),
                customer.getLastPaymentMethod(),
                customer.isAffiliated(),
                customer.isDataConsentAccepted(),
                customer.getDataConsentDate() != null ? customer.getDataConsentDate().toString() : null,
                customer.getPointsBalance()
        );
    }
}
