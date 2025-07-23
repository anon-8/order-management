package com.company.manufacturingorder.application.command;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.domain.model.OrderStatus;

public record ChangeOrderStatusCommand(
    OrderId orderId,
    OrderStatus newStatus
) {
    public ChangeOrderStatusCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
    }
}