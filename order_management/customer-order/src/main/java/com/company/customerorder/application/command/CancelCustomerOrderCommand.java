package com.company.customerorder.application.command;

import com.company.sharedkernel.OrderId;

public record CancelCustomerOrderCommand(
    OrderId orderId,
    String reason
) {
    public CancelCustomerOrderCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason cannot be null or empty");
        }
    }
}