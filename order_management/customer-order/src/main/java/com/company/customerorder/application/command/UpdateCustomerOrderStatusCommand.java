package com.company.customerorder.application.command;

import com.company.sharedkernel.OrderId;
import com.company.customerorder.domain.model.CustomerOrderStatus;

public record UpdateCustomerOrderStatusCommand(
    OrderId orderId,
    CustomerOrderStatus newStatus
) {
    public UpdateCustomerOrderStatusCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
    }
}