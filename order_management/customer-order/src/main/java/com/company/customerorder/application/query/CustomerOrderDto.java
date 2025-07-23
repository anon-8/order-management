package com.company.customerorder.application.query;

import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.CustomerOrderStatus;

import java.time.Instant;
import java.util.List;

public record CustomerOrderDto(
    OrderId orderId,
    CustomerId customerId,
    String customerName,
    String customerEmail,
    String customerAddress,
    List<OrderItemDto> items,
    Money totalAmount,
    CustomerOrderStatus status,
    Instant placedAt,
    Instant updatedAt,
    OrderId manufacturingOrderId,
    boolean isActive
) {
    
    public record OrderItemDto(
        String productCode,
        String description,
        Integer quantity,
        Money unitPrice,
        Money totalPrice
    ) {}
}