package com.company.manufacturingorder.application.port;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.domain.model.OrderStatus;

import java.util.Optional;

public interface ManufacturingOrderQueryPort {
    
    Optional<ManufacturingOrderStatus> getOrderStatus(OrderId orderId);
    
    boolean isOrderCompleted(OrderId orderId);
    
    record ManufacturingOrderStatus(
        OrderId orderId,
        OrderStatus status,
        String productCode,
        Integer quantity
    ) {}
}