package com.company.customerorder.application.command;

import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.customerorder.domain.model.CustomerOrder;
import com.company.customerorder.domain.model.CustomerInfo;
import com.company.customerorder.domain.model.OrderItem;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import com.company.customerorder.domain.service.CustomerOrderDomainService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaceCustomerOrderHandler {
    private static final Logger log = LoggerFactory.getLogger(PlaceCustomerOrderHandler.class);
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    
    private final CustomerOrderRepository repository;
    private final CustomerOrderDomainService domainService;
    
    @Transactional
    public OrderId handle(PlaceCustomerOrderCommand command) {
        log.info("Starting customer order placement: orderId={}, customerId={}, itemCount={}", 
                command.orderId(), command.customerId(), command.items().size());
        
        long startTime = System.currentTimeMillis();
        
        try {
            var customerInfo = CustomerInfo.of(
                command.customerId(),
                command.customerName(),
                command.customerEmail(),
                command.customerAddress()
            );
            
            var orderItems = command.items().stream()
                .map(item -> OrderItem.of(
                    item.productCode(),
                    item.description(),
                    item.quantity(),
                    Money.of(item.unitPrice(), item.currency())
                ))
                .toList();
            
            log.debug("Validating order items for orderId={}", command.orderId());
            domainService.validateOrderItems(orderItems);
            
            var order = CustomerOrder.placeOrder(
                command.orderId(),
                customerInfo,
                orderItems
            );
            
            repository.save(order);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully placed customer order: orderId={}, totalAmount={}, duration={}ms", 
                    command.orderId(), order.getTotalAmount(), duration);
            
            auditLog.info("CUSTOMER_ORDER_PLACED orderId={} customerId={} totalAmount={} itemCount={} duration={}ms", 
                    command.orderId(), command.customerId(), order.getTotalAmount(), 
                    command.items().size(), duration);
            
            return order.getId();
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed to place customer order: orderId={}, customerId={}, duration={}ms, error={}", 
                    command.orderId(), command.customerId(), duration, e.getMessage(), e);
            
            auditLog.error("CUSTOMER_ORDER_PLACEMENT_FAILED orderId={} customerId={} error={} duration={}ms", 
                    command.orderId(), command.customerId(), e.getMessage(), duration);
            throw e;
        }
    }
}