package com.company.sharedkernel.events;

import com.company.sharedkernel.DomainEvent;
import com.company.sharedkernel.OrderId;

import java.time.Instant;

public record CustomerOrderCancelled(
    OrderId orderId,
    String reason,
    Instant occurredOn
) implements DomainEvent {
    
    public static CustomerOrderCancelled of(OrderId orderId, String reason) {
        return new CustomerOrderCancelled(orderId, reason, Instant.now());
    }
    
    @Override
    public String eventType() {
        return "CustomerOrderCancelled";
    }
}