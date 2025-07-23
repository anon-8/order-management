package com.company.sharedkernel.events;

import com.company.sharedkernel.DomainEvent;
import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;

import java.time.Instant;
import java.util.UUID;

public record CustomerOrderPlaced(
    OrderId orderId,
    UUID customerId,
    Money totalAmount,
    Instant occurredOn
) implements DomainEvent {
    
    public static CustomerOrderPlaced of(OrderId orderId, UUID customerId, Money totalAmount) {
        return new CustomerOrderPlaced(orderId, customerId, totalAmount, Instant.now());
    }
    
    @Override
    public String eventType() {
        return "CustomerOrderPlaced";
    }
}