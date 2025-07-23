package com.company.sharedkernel.events;

import com.company.sharedkernel.DomainEvent;
import com.company.sharedkernel.OrderId;

import java.time.Instant;

public record ManufacturingOrderStatusChanged(
    OrderId orderId,
    String previousStatus,
    String newStatus,
    Instant occurredOn
) implements DomainEvent {
    
    public static ManufacturingOrderStatusChanged of(OrderId orderId, String previousStatus, String newStatus) {
        return new ManufacturingOrderStatusChanged(orderId, previousStatus, newStatus, Instant.now());
    }
    
    @Override
    public String eventType() {
        return "ManufacturingOrderStatusChanged";
    }
}