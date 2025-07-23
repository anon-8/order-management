package com.company.customerorder.domain.port;

import com.company.sharedkernel.OrderId;
import com.company.customerorder.domain.model.CustomerOrder;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.CustomerOrderStatus;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository {
    
    Optional<CustomerOrder> findById(OrderId orderId);
    
    List<CustomerOrder> findByCustomerId(CustomerId customerId);
    
    List<CustomerOrder> findByStatus(CustomerOrderStatus status);
    
    List<CustomerOrder> findActiveOrders();
    
    List<CustomerOrder> findAll();
    
    List<CustomerOrder> findByManufacturingOrderId(OrderId manufacturingOrderId);
    
    CustomerOrder save(CustomerOrder order);
    
    void delete(OrderId orderId);
    
    boolean existsById(OrderId orderId);
}