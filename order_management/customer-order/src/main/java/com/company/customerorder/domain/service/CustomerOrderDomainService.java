package com.company.customerorder.domain.service;

import com.company.customerorder.domain.model.CustomerOrder;
import com.company.customerorder.domain.model.OrderItem;
import com.company.customerorder.domain.port.ManufacturingOrderQueryPort;
import com.company.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.List;

public class CustomerOrderDomainService {
    private final ManufacturingOrderQueryPort manufacturingOrderQueryPort;
    
    public CustomerOrderDomainService(ManufacturingOrderQueryPort manufacturingOrderQueryPort) {
        this.manufacturingOrderQueryPort = manufacturingOrderQueryPort;
    }
    
    public void validateOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        for (OrderItem item : items) {
            validateOrderItem(item);
        }
    }
    
    public Money calculateOrderDiscount(CustomerOrder order) {
        Money totalAmount = order.getTotalAmount();
        
        if (totalAmount.getAmount().compareTo(new BigDecimal("1000")) >= 0) {
            return totalAmount.multiply(new BigDecimal("0.10"));
        } else if (totalAmount.getAmount().compareTo(new BigDecimal("500")) >= 0) {
            return totalAmount.multiply(new BigDecimal("0.05"));
        }
        
        return Money.zero(totalAmount.getCurrency());
    }
    
    public boolean canCancelOrder(CustomerOrder order) {
        if (order.isCancelled() || order.isDelivered()) {
            return false;
        }
        
        if (order.getManufacturingOrderId() != null) {
            var manufacturingStatus = manufacturingOrderQueryPort
                .getOrderStatus(order.getManufacturingOrderId());
            
            return manufacturingStatus.map(status -> 
                !"COMPLETED".equals(status.status()) && !"IN_PROGRESS".equals(status.status())
            ).orElse(true);
        }
        
        return true;
    }
    
    private void validateOrderItem(OrderItem item) {
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Item quantity must be positive");
        }
        
        if (!item.getUnitPrice().isPositive()) {
            throw new IllegalArgumentException("Item unit price must be positive");
        }
    }
}