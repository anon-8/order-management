package com.company.customerorder.domain.port;

import com.company.sharedkernel.OrderId;

import java.util.Optional;

public interface ManufacturingOrderQueryPort {
    
    Optional<ManufacturingOrderStatus> getOrderStatus(OrderId manufacturingOrderId);
    
    boolean isOrderCompleted(OrderId manufacturingOrderId);
    
    record ManufacturingOrderStatus(
        OrderId orderId,
        String status,
        String productCode,
        Integer quantity
    ) {}
}