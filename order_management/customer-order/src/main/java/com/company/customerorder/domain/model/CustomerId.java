package com.company.customerorder.domain.model;

import com.company.sharedkernel.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class CustomerId extends ValueObject {
    private final UUID value;
    
    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }
    
    public static CustomerId of(String value) {
        return new CustomerId(UUID.fromString(value));
    }
    
    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}