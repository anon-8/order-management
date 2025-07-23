package com.company.customerorder.application.query;

import com.company.sharedkernel.OrderId;

public record FindCustomerOrderQuery(
    OrderId orderId
) {
    public FindCustomerOrderQuery {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
    }
}