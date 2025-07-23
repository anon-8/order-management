package com.company.sharedkernel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class OrderId extends ValueObject {
    private final UUID value;
    
    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }
    
    public static OrderId of(String value) {
        return new OrderId(UUID.fromString(value));
    }
    
    public static OrderId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("OrderId UUID cannot be null");
        }
        return new OrderId(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}