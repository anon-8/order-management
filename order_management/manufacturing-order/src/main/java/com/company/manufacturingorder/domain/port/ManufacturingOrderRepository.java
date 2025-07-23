package com.company.manufacturingorder.domain.port;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface ManufacturingOrderRepository {
    
    Optional<ManufacturingOrder> findById(OrderId orderId);
    
    List<ManufacturingOrder> findByStatus(OrderStatus status);
    
    List<ManufacturingOrder> findOverdueOrders();
    
    List<ManufacturingOrder> findAll();
    
    ManufacturingOrder save(ManufacturingOrder order);
    
    void delete(OrderId orderId);
    
    boolean existsById(OrderId orderId);
}