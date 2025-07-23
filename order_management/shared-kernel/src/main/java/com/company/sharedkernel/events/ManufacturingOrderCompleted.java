package com.company.sharedkernel.events;

import com.company.sharedkernel.DomainEvent;
import com.company.sharedkernel.OrderId;

import java.time.Instant;

public record ManufacturingOrderCompleted(
    OrderId orderId,
    Instant completedAt,
    Instant occurredOn
) implements DomainEvent {
    
    public static ManufacturingOrderCompleted of(OrderId orderId, Instant completedAt) {
        return new ManufacturingOrderCompleted(orderId, completedAt, Instant.now());
    }
    
    @Override
    public String eventType() {
        return "ManufacturingOrderCompleted";
    }
}