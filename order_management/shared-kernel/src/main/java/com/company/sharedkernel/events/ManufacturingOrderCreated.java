package com.company.sharedkernel.events;

import com.company.sharedkernel.DomainEvent;
import com.company.sharedkernel.OrderId;

import java.time.Instant;

public record ManufacturingOrderCreated(
    OrderId orderId,
    String productCode,
    Integer quantity,
    Instant occurredOn
) implements DomainEvent {
    
    public static ManufacturingOrderCreated of(OrderId orderId, String productCode, Integer quantity) {
        return new ManufacturingOrderCreated(orderId, productCode, quantity, Instant.now());
    }
    
    @Override
    public String eventType() {
        return "ManufacturingOrderCreated";
    }
}