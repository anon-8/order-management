package com.company.manufacturingorder.application.command;

import com.company.sharedkernel.OrderId;

public record CompleteManufacturingOrderCommand(
    OrderId orderId
) {
    public CompleteManufacturingOrderCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
    }
}