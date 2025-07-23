package com.company.customerorder.domain.model;

import com.company.sharedkernel.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class CustomerInfo extends ValueObject {
    private final CustomerId customerId;
    private final String name;
    private final String email;
    private final String address;
    
    public static CustomerInfo of(CustomerId customerId, String name, String email, String address) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        
        return new CustomerInfo(customerId, name.trim(), email.trim(), address.trim());
    }
}