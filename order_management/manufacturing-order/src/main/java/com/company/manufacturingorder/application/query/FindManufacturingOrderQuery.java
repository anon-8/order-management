package com.company.manufacturingorder.application.query;

import com.company.sharedkernel.OrderId;

public record FindManufacturingOrderQuery(
    OrderId orderId
) {
    public FindManufacturingOrderQuery {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
    }
}