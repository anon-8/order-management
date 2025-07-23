package com.company.manufacturingorder.domain.service;

import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;

import java.util.List;

public class ManufacturingOrderDomainService {
    private final ManufacturingOrderRepository repository;
    
    public ManufacturingOrderDomainService(ManufacturingOrderRepository repository) {
        this.repository = repository;
    }
    
    public boolean canScheduleNewOrder() {
        List<ManufacturingOrder> inProgressOrders = repository.findByStatus(OrderStatus.IN_PROGRESS);
        return inProgressOrders.size() < getMaxConcurrentOrders();
    }
    
    public List<ManufacturingOrder> findOrdersRequiringAttention() {
        return repository.findOverdueOrders();
    }
    
    public void validateOrderScheduling(ManufacturingOrder order) {
        if (!canScheduleNewOrder()) {
            throw new IllegalStateException("Cannot schedule new order: maximum concurrent orders reached");
        }
        
        if (order.getTimeline().getExpectedStartDate().isBefore(java.time.Instant.now())) {
            throw new IllegalArgumentException("Cannot schedule order with start date in the past");
        }
    }
    
    private int getMaxConcurrentOrders() {
        return 10;
    }
}