package com.company.customerorder.application.command;

import com.company.customerorder.domain.port.CustomerOrderRepository;
import com.company.customerorder.domain.service.CustomerOrderDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CancelCustomerOrderHandler {
    private final CustomerOrderRepository repository;
    private final CustomerOrderDomainService domainService;
    
    @Transactional
    public void handle(CancelCustomerOrderCommand command) {
        var order = repository.findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Customer order not found: " + command.orderId()
            ));
        
        if (!domainService.canCancelOrder(order)) {
            throw new IllegalStateException("Order cannot be cancelled at this time");
        }
        
        order.cancel(command.reason());
        
        repository.save(order);
    }
}